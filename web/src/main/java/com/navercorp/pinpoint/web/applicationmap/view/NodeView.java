package com.navercorp.pinpoint.web.applicationmap.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.navercorp.pinpoint.common.server.util.json.JacksonWriterUtils;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonSerialize(using = NodeView.NodeViewSerializer.class)
public class NodeView {
    private final Node node;
    private final MapViews activeView;
    private final HyperLinkFactory hyperLinkFactory;
    private final TimeHistogramFormat format;

    public NodeView(Node node, MapViews activeView, HyperLinkFactory hyperLinkFactory, TimeHistogramFormat format) {
        this.node = Objects.requireNonNull(node, "node");
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.format = Objects.requireNonNull(format, "format");
    }

    private Node getNode() {
        return node;
    }

    public MapViews getActiveView() {
        return activeView;
    }

    public HyperLinkFactory getHyperLinkFactory() {
        return hyperLinkFactory;
    }

    private TimeHistogramFormat getFormat() {
        return format;
    }

    static class NodeViewSerializer extends JsonSerializer<NodeView> {

        public static final String AGENT_TIME_SERIES_HISTOGRAM = "agentTimeSeriesHistogram";

        @Override
        public void serialize(NodeView nodeView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            Node node = nodeView.getNode();

            jgen.writeStartObject();
//        jgen.writeStringField("id", node.getNodeName());serverInstanceList
            jgen.writeObjectField("key", node.getNodeName()); // necessary for go.js

            jgen.writeStringField("applicationName", node.getApplicationTextName()); // for go.js

            jgen.writeStringField("category", node.getServiceType().toString());  // necessary for go.js
            jgen.writeStringField("serviceType", node.getServiceType().toString());

            final ServiceType serviceType = node.getApplication().getServiceType();
//        if (serviceType.isUser()) {
//            jgen.writeStringField("fig", "Ellipse");
//        } else if(serviceType.isWas()) {
//            jgen.writeStringField("fig", "RoundedRectangle");
//        } else {
//            jgen.writeStringField("fig", "Rectangle");
//        }

            jgen.writeNumberField("serviceTypeCode", serviceType.getCode());
//        jgen.writeStringField("terminal", Boolean.toString(serviceType.isTerminal()));
            jgen.writeBooleanField("isWas", serviceType.isWas());  // for go.js
            jgen.writeBooleanField("isQueue", serviceType.isQueue());
            jgen.writeBooleanField("isAuthorized", node.isAuthorized());

            writeHistogram(nodeView, jgen, provider);
            writeServerGroupList(nodeView, jgen);

            jgen.writeEndObject();
        }


        private void writeServerGroupList(NodeView nodeView, JsonGenerator jgen) throws IOException {
            final Node node = nodeView.getNode();
            ServerGroupList serverGroupList = node.getServerGroupList();
            if (node.getServiceType().isUnknown()) {
                serverGroupList = null;
            }

            final String agentIdNameMapKey = "agentIdNameMap";
            final MapViews activeView = nodeView.getActiveView();
            if (serverGroupList == null) {
                jgen.writeNumberField("instanceCount", 0);
                jgen.writeNumberField("instanceErrorCount", 0);
                JacksonWriterUtils.writeEmptyArray(jgen, "agentIds");
                JacksonWriterUtils.writeEmptyObject(jgen, agentIdNameMapKey);

                if (activeView.isDetailed()) {
                    JacksonWriterUtils.writeEmptyObject(jgen, "serverList");
                }
            } else {
                jgen.writeNumberField("instanceCount", serverGroupList.getInstanceCount());
                long instanceErrorCount = 0;
                NodeHistogram nodeHistogram = node.getNodeHistogram();
                if (nodeHistogram != null) {
                    Map<String, Histogram> agentHistogramMap = node.getNodeHistogram().getAgentHistogramMap();
                    if (agentHistogramMap != null) {
                        instanceErrorCount = agentHistogramMap.values().stream()
                                .filter(agentHistogram -> agentHistogram.getTotalErrorCount() > 0)
                                .count();
                    }
                }
                jgen.writeNumberField("instanceErrorCount", instanceErrorCount);
                jgen.writeArrayFieldStart("agentIds");
                for (String agentId : serverGroupList.getAgentIdList()) {
                    jgen.writeString(agentId);
                }
                jgen.writeEndArray();

                jgen.writeObjectFieldStart(agentIdNameMapKey);
                for (Map.Entry<String, String> entry : serverGroupList.getAgentIdNameMap().entrySet()) {
                    jgen.writeStringField(entry.getKey(), entry.getValue());
                }
                jgen.writeEndObject();

                if (activeView.isDetailed()) {
                    jgen.writeObjectField("serverList", new ServerGroupListView(serverGroupList, nodeView.getHyperLinkFactory()));
                }
            }
        }

        private void writeHistogram(NodeView nodeView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            final Node node = nodeView.getNode();
            final ServiceType serviceType = node.getServiceType();
            final NodeHistogram nodeHistogram = node.getNodeHistogram();
            final MapViews activeView = nodeView.getActiveView();

            // FIXME isn't this all ServiceTypes that can be a node?
            final boolean nodeServiceType = isNodeServiceType(serviceType);
            if (nodeServiceType) {
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                if (applicationHistogram == null) {
                    jgen.writeBooleanField("hasAlert", false);  // for go.js
                } else {
                    jgen.writeNumberField("totalCount", applicationHistogram.getTotalCount()); // for go.js
                    jgen.writeNumberField("errorCount", applicationHistogram.getTotalErrorCount());
                    jgen.writeNumberField("slowCount", applicationHistogram.getSlowCount());

                    jgen.writeBooleanField("hasAlert", hasAlert(applicationHistogram));  // for go.js
                }

                ResponseTimeStatics responseTimeStatics = ResponseTimeStatics.fromHistogram(applicationHistogram);
                jgen.writeObjectField(ResponseTimeStatics.RESPONSE_STATISTICS, responseTimeStatics);
                if (applicationHistogram == null) {
                    JacksonWriterUtils.writeEmptyObject(jgen, "histogram");
                } else {
                    jgen.writeObjectField("histogram", applicationHistogram);
                }

                if (activeView.isSimplified()) {
                    if (applicationHistogram == null) {
                        JacksonWriterUtils.writeEmptyObject(jgen, "apdexScore");
                    } else {
                        //jgen.writeObjectField("apdexScore", node.getApdexScore());
                        JsonSerializer<Object> beanSerializer = provider.findValueSerializer(node.getApdexScore().getClass());
                        JsonSerializer<Object> unwrapping = beanSerializer.unwrappingSerializer(NameTransformer.NOP);
                        unwrapping.serialize(node.getApdexScore(), jgen, provider);
                    }
                }

                // agent histogram
                if (activeView.isDetailed()) {
                    Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
                    if (agentHistogramMap == null) {
                        JacksonWriterUtils.writeEmptyObject(jgen, "agentHistogram");
                        JacksonWriterUtils.writeEmptyObject(jgen, ResponseTimeStatics.AGENT_RESPONSE_STATISTICS);
                    } else {
                        jgen.writeObjectField("agentHistogram", agentHistogramMap);
                        jgen.writeObjectField(ResponseTimeStatics.AGENT_RESPONSE_STATISTICS, nodeHistogram.getAgentResponseStatisticsMap());
                    }
                }
            } else {
                jgen.writeBooleanField("hasAlert", false);  // for go.js
            }

            //time histogram
            if (!activeView.isSimplified()) {
                // FIXME isn't this all ServiceTypes that can be a node?
                if (nodeServiceType) {
                    final TimeHistogramFormat format = nodeView.getFormat();

                    ApplicationTimeHistogram applicationTimeHistogram = nodeHistogram.getApplicationTimeHistogram();
                    TimeHistogramBuilder builder = new TimeHistogramBuilder(format);
                    List<TimeHistogramViewModel> applicationTimeSeriesHistogram = builder.build(applicationTimeHistogram);
                    if (applicationTimeSeriesHistogram == null) {
                        JacksonWriterUtils.writeEmptyArray(jgen, "timeSeriesHistogram");
                    } else {
                        jgen.writeObjectField("timeSeriesHistogram", applicationTimeSeriesHistogram);
                    }

                    if (activeView.isDetailed()) {
                        AgentTimeHistogram agentTimeHistogram = nodeHistogram.getAgentTimeHistogram();
                        JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentFields = agentTimeHistogram.createViewModel(format);
                        jgen.writeFieldName(AGENT_TIME_SERIES_HISTOGRAM);
                        jgen.writeObject(agentFields);
                    }
                }
            }
        }

        private boolean isNodeServiceType(ServiceType serviceType) {
            return serviceType.isWas() || serviceType.isTerminal() || serviceType.isUser() || serviceType.isUnknown() || serviceType.isQueue() || serviceType.isAlias();
        }

        private boolean hasAlert(Histogram applicationHistogram) {
            final long totalCount = applicationHistogram.getTotalCount();
            if (totalCount == 0) {
                return false;
            } else {
                long error = applicationHistogram.getTotalErrorCount() / totalCount;
                return error * 100 > 10;
            }
        }

    }
}
