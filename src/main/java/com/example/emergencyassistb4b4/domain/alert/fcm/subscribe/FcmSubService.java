package com.example.emergencyassistb4b4.domain.alert.fcm.subscribe;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmSubService {

    @Value("${fcm.topics.threshold}")
    private String thresholdTopic;

    private final FirebaseMessaging fcm;

    public void subscribeNgoTokens(List<String> tokens) throws FirebaseMessagingException {
        fcm.subscribeToTopic(tokens, thresholdTopic);
    }

    public void unsubscribeNgoTokens(List<String> tokens) throws FirebaseMessagingException {
        fcm.unsubscribeFromTopic(tokens, thresholdTopic);
    }

}
