import { useAtomValue } from 'jotai';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { AgentStatisticPage as CommonAgentStatisticPage, withInitialFetch } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export interface AgentStatisticPageProps {}
const AgentStatisticPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return <CommonAgentStatisticPage configuration={configuration} />;
};

export default withInitialFetch((props: AgentStatisticPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<AgentStatisticPage {...props} />)),
);
