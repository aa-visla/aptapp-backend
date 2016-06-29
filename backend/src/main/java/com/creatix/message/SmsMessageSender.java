package com.creatix.message;

import com.creatix.configuration.TwilioProperties;
import com.twilio.sdk.Twilio;
import com.twilio.sdk.type.PhoneNumber;
import com.twilio.sdk.resource.api.v2010.account.Message;
import com.twilio.sdk.creator.api.v2010.account.MessageCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsMessageSender {

    private TwilioProperties twilioProperties;

    @Autowired
    public SmsMessageSender(TwilioProperties twilioProperties) {
        this.twilioProperties = twilioProperties;

        Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());
    }

    /**
     * Send SMS message to phone number.
     *
     * @param body SMS message text. Example: "Hello from Java"
     * @param recipientPhone SMS recipient phone number. Example: "+12345678901"
     */
    public void send(String body, String recipientPhone) throws MessageDeliveryException {
        Message message = new MessageCreator(
                twilioProperties.getAccountSid(),
                new PhoneNumber(recipientPhone),
                new PhoneNumber(twilioProperties.getFrom()),
                body
        ).execute();

        if ( message.getStatus() == Message.Status.FAILED ) {
            throw new MessageDeliveryException(String.format("SMS delivery failed. Error %d: %s", message.getErrorCode(), message.getErrorMessage()));
        }
    }

}
