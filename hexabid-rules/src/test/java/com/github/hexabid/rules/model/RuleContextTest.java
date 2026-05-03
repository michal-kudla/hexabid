package com.github.hexabid.rules.model;

import com.github.hexabid.rules.ast.AttributeKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleContextTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        void buildsContextWithAllConvenienceMethods() {
            var ctx = RuleContext.builder()
                .productType("REAL_ESTATE")
                .estimatedValue(new BigDecimal("50000"))
                .bidderAge(25)
                .wadiumPaid(true)
                .excisable(false)
                .imported(true)
                .customsExempt(false)
                .kycVerified(true)
                .viewingDateScheduled(true)
                .exciseDocumentStatus("ORIGINAL")
                .customsExemptionDocStatus("COPY")
                .settlementDocumentProvided(true)
                .daysSinceClose(2)
                .fullPaymentReceived(false)
                .receiptConfirmed(true)
                .build();

            assertThat(ctx.getString(AttributeKey.PRODUCT_TYPE)).isEqualTo("REAL_ESTATE");
            assertThat(ctx.getBigDecimal(AttributeKey.ESTIMATED_VALUE)).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(ctx.getInteger(AttributeKey.BIDDER_AGE)).isEqualTo(25);
            assertThat(ctx.getBoolean(AttributeKey.WADIUM_PAID)).isTrue();
            assertThat(ctx.getBoolean(AttributeKey.EXCISABLE)).isFalse();
            assertThat(ctx.getBoolean(AttributeKey.IMPORTED)).isTrue();
            assertThat(ctx.getBoolean(AttributeKey.CUSTOMS_EXEMPT)).isFalse();
            assertThat(ctx.getBoolean(AttributeKey.KYC_VERIFIED)).isTrue();
            assertThat(ctx.getBoolean(AttributeKey.VIEWING_DATE_SCHEDULED)).isTrue();
            assertThat(ctx.getString(AttributeKey.EXCISE_DOCUMENT_STATUS)).isEqualTo("ORIGINAL");
            assertThat(ctx.getString(AttributeKey.CUSTOMS_EXEMPTION_DOC_STATUS)).isEqualTo("COPY");
            assertThat(ctx.getBoolean(AttributeKey.SETTLEMENT_DOCUMENT_PROVIDED)).isTrue();
            assertThat(ctx.getInteger(AttributeKey.DAYS_SINCE_CLOSE)).isEqualTo(2);
            assertThat(ctx.getBoolean(AttributeKey.FULL_PAYMENT_RECEIVED)).isFalse();
            assertThat(ctx.getBoolean(AttributeKey.RECEIPT_CONFIRMED)).isTrue();
        }

        @Test
        void builderWithGenericWith() {
            var ctx = RuleContext.builder()
                .with(AttributeKey.PRODUCT_TYPE, "VEHICLE")
                .build();
            assertThat(ctx.getString(AttributeKey.PRODUCT_TYPE)).isEqualTo("VEHICLE");
        }

        @Test
        void builderRejectsNullKey() {
            assertThatThrownBy(() -> RuleContext.builder().with(null, "value"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void builderRejectsNullValue() {
            assertThatThrownBy(() -> RuleContext.builder().with(AttributeKey.PRODUCT_TYPE, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void emptyContextReturnsNullForGet() {
            var ctx = RuleContext.builder().build();
            assertThat(ctx.get(AttributeKey.PRODUCT_TYPE)).isNull();
        }
    }

    @Nested
    @DisplayName("Typed getters")
    class TypedGetters {

        @Test
        void getStringReturnsNullForWrongType() {
            var ctx = RuleContext.builder().bidderAge(25).build();
            assertThat(ctx.getString(AttributeKey.BIDDER_AGE)).isNull();
        }

        @Test
        void getBigDecimalReturnsNullForWrongType() {
            var ctx = RuleContext.builder().productType("ALCOHOL").build();
            assertThat(ctx.getBigDecimal(AttributeKey.PRODUCT_TYPE)).isNull();
        }

        @Test
        void getIntegerReturnsNullForWrongType() {
            var ctx = RuleContext.builder().productType("ALCOHOL").build();
            assertThat(ctx.getInteger(AttributeKey.PRODUCT_TYPE)).isNull();
        }

        @Test
        void getBooleanReturnsNullForWrongType() {
            var ctx = RuleContext.builder().bidderAge(25).build();
            assertThat(ctx.getBoolean(AttributeKey.BIDDER_AGE)).isNull();
        }
    }

    @Nested
    @DisplayName("has() method")
    class HasMethod {

        @Test
        void hasReturnsTrueWhenKeyPresent() {
            var ctx = RuleContext.builder().productType("ALCOHOL").build();
            assertThat(ctx.has(AttributeKey.PRODUCT_TYPE)).isTrue();
        }

        @Test
        void hasReturnsFalseWhenKeyAbsent() {
            var ctx = RuleContext.builder().productType("ALCOHOL").build();
            assertThat(ctx.has(AttributeKey.WADIUM_PAID)).isFalse();
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        void estimatedValueLongOverload() {
            var ctx = RuleContext.builder().estimatedValue(50000L).build();
            assertThat(ctx.getBigDecimal(AttributeKey.ESTIMATED_VALUE)).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        void attributesReturnsUnmodifiableMap() {
            var ctx = RuleContext.builder().productType("ALCOHOL").build();
            assertThatThrownBy(() -> ctx.attributes().put(AttributeKey.PRODUCT_TYPE, "TAMPERED"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
