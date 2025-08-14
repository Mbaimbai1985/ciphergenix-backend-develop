package com.sphesihlemhlongo.paymentservicepractice.service;

import com.sphesihlemhlongo.paymentservicepractice.dto.PaymentRequest;
import com.sphesihlemhlongo.paymentservicepractice.dto.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    public StripeResponse checkoutSubscription(PaymentRequest paymentRequest){
        Stripe.apiKey =stripeSecretKey;

        StripeResponse stripeResponse = new StripeResponse();

        SessionCreateParams.LineItem.PriceData.ProductData subscriptionData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(paymentRequest.getName())
                .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(paymentRequest.getCurrency()== null ? "USD": paymentRequest.getCurrency())
                    .setUnitAmount(paymentRequest.getAmount())
                    .setProductData(subscriptionData)
                    .build();

        SessionCreateParams.LineItem lineitem =
                SessionCreateParams
                        .LineItem.builder()
                        .setQuantity(paymentRequest.getQuantity())
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/success")
                .setCancelUrl("http://localhost:8080/cancel")
                .addLineItem(lineitem)
                .build();

        Session session = new Session();
        try{
            session = Session.create(params);
        }catch (StripeException exception){
//            System.out.println(exception.getMessage());
        }
    return StripeResponse
            .builder()
            .status(stripeResponse.getStatus())
            .message(stripeResponse.getMessage())
            .sessionId(session.getId())
            .sessionUrl(session.getUrl())
            .build();
    }

}
