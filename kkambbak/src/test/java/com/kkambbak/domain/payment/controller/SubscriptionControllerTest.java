package com.kkambbak.domain.payment.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.kkambbak.KkambbakDocumentApiTester;
import com.kkambbak.core.entity.payment.enums.SubscriptionStatus;
import com.kkambbak.domain.payment.dto.SubscriptionDto;
import com.kkambbak.domain.payment.dto.SubscriptionPlanDto;
import com.kkambbak.domain.payment.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubscriptionControllerTest extends KkambbakDocumentApiTester {

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    void getActiveSubscriptionTest() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        SubscriptionDto mockResponse = SubscriptionDto.builder()
                .subscriptionId(1L)
                .planName("Premium Plan")
                .planAmount(9900L)
                .planDurationDays(30)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(now.plusDays(30))
                .autoRenew(true)
                .hasActiveBillingKey(true)
                .build();
        given(subscriptionService.getActiveSubscription(anyLong())).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/subscriptions/active")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("subscription-get-active",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Subscriptions")
                                        .summary("내 활성화된 구독 조회")
                                        .description("사용자의 현재 활성화 구독 정보를 조회합니다. 구독이 없으면 예외처리 됩니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                fieldWithPath("body.subscriptionId").type(JsonFieldType.NUMBER).description("구독 ID"),
                                                fieldWithPath("body.planName").type(JsonFieldType.STRING).description("플랜명"),
                                                fieldWithPath("body.planAmount").type(JsonFieldType.NUMBER).description("월 결제액"),
                                                fieldWithPath("body.planDurationDays").type(JsonFieldType.NUMBER).description("구독 기간 (일)"),
                                                fieldWithPath("body.status").type(JsonFieldType.STRING).description("구독 상태"),
                                                fieldWithPath("body.startDate").type(JsonFieldType.STRING).description("구독 시작일"),
                                                fieldWithPath("body.endDate").type(JsonFieldType.STRING).description("구독 종료일"),
                                                fieldWithPath("body.autoRenew").type(JsonFieldType.BOOLEAN).description("자동 갱신 여부"),
                                                fieldWithPath("body.hasActiveBillingKey").type(JsonFieldType.BOOLEAN).description("정기결제 등록 여부")
                                        )
                                        .build()
                        )
                ));
    }


    @Test
    void cancelSubscriptionTest() throws Exception {
        // given
        Long subscriptionId = 1L;
        doNothing().when(subscriptionService).cancelSubscription(anyLong(), anyLong());

        // when & then
        this.mockMvc.perform(post("/api/v1/subscriptions/{subscriptionId}/cancel", subscriptionId)
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("subscription-cancel",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Subscriptions")
                                        .summary("구독 취소")
                                        .description("사용자의 활성 구독을 취소합니다. 취소 후 더 이상 결제되지 않으며 현재 구독 기간은 유지됩니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .pathParameters(
                                                parameterWithName("subscriptionId").description("구독 ID")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.NULL).description("응답 데이터 (없음)").optional()
                                        )
                                        .build()
                        )
                ));
    }

    @Test
    void getAllSubscriptionPlansTest() throws Exception {
        // given
        List<SubscriptionPlanDto> mockResponse = Arrays.asList(
                SubscriptionPlanDto.builder()
                        .id(1L)
                        .name("Basic")
                        .price(4900L)
                        .build(),
                SubscriptionPlanDto.builder()
                        .id(2L)
                        .name("Premium")
                        .price(9900L)
                        .build()
        );
        given(subscriptionService.getAllSubscriptionPlans()).willReturn(mockResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/subscriptions/plans")
                        .header("Authorization", "Bearer access_token_example")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(document("subscription-get-all-plans",
                        resource(
                                ResourceSnippetParameters.builder()
                                        .tag("Subscriptions")
                                        .summary("구독 상품 조회")
                                        .description("구독 상품 목록을 조회합니다.")
                                        .requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰")
                                        )
                                        .responseFields(
                                                fieldWithPath("status.statusCode").type(JsonFieldType.STRING).description("상태 코드"),
                                                fieldWithPath("status.message").type(JsonFieldType.STRING).description("상태 메시지"),
                                                fieldWithPath("status.description").type(JsonFieldType.STRING).description("상태 설명").optional(),
                                                fieldWithPath("body").type(JsonFieldType.ARRAY).description("구독 상품 목록"),
                                                fieldWithPath("body[].id").type(JsonFieldType.NUMBER).description("구독 상품 ID"),
                                                fieldWithPath("body[].name").type(JsonFieldType.STRING).description("구독 상품 이름"),
                                                fieldWithPath("body[].price").type(JsonFieldType.NUMBER).description("구독 상품 가격")
                                        )
                                        .build()
                        )
                ));
    }
}