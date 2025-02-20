import React from 'react';
import { Webhook } from '@pinpoint-fe/ui/src/constants';
import { useGetWebhook } from '@pinpoint-fe/ui/src/hooks';

export interface WebhookListFetcherProps extends Webhook.Parameters {
  disableFetch?: boolean;
  empty?: React.ReactNode;
  children?: (webhookList?: Webhook.WebhookData[] | null) => React.ReactNode;
}

export const WebhookListFetcher = ({
  disableFetch,
  children,
  empty = 'No datas.',
  ...props
}: WebhookListFetcherProps) => {
  const { data: webhookList } = useGetWebhook({
    applicationId: props?.applicationId,
    disableFetch,
    suspense: true,
  });

  return (
    <>
      {children
        ? children(webhookList)
        : webhookList && webhookList.length > 0
          ? webhookList.map((webhook) => webhook.alias)
          : empty}
    </>
  );
};
