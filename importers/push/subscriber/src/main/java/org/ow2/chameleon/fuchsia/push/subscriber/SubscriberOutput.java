package org.ow2.chameleon.fuchsia.push.subscriber;


import org.ow2.chameleon.fuchsia.pubsubhub.hub.dto.SubscriptionRequest;

import java.util.ArrayList;
import java.util.List;

public interface SubscriberOutput {

    public int subscribe(String hub, String topic_url,String hostname,String verifytoken,String lease_seconds) throws Exception;

    public int unsubscribe(String hub, String topic_url,String hostname,String verifytoken) throws Exception;

    public List<String> getApprovedActions();

}
