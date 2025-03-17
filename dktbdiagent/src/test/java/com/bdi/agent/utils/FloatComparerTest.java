package com.bdi.agent.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.bdi.agent.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class FloatComparerTest {

    private transient final FloatComparer floatComparer;

    @Autowired
    public FloatComparerTest(FloatComparer floatComparer) {
        this.floatComparer = floatComparer;
    }

    @Test
    public void testEqualToTrue() {
        BigDecimal value = BigDecimal.valueOf(0.5);
        BigDecimal delta = BigDecimal.valueOf(0.00009);

        assertThat(floatComparer.equalTo(value.floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.equalTo(value.add(delta).floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.equalTo(value.floatValue(), value.add(delta).floatValue())).isTrue();
        assertThat(floatComparer.equalTo(value.subtract(delta).floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.equalTo(value.floatValue(), value.subtract(delta).floatValue())).isTrue();
    }

    @Test
    public void testEqualToFalse() {
        BigDecimal value = BigDecimal.valueOf(0.5);
        BigDecimal delta = BigDecimal.valueOf(0.00011);

        assertThat(floatComparer.equalTo(value.add(delta).floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.equalTo(value.floatValue(), value.add(delta).floatValue())).isFalse();
        assertThat(floatComparer.equalTo(value.subtract(delta).floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.equalTo(value.floatValue(), value.subtract(delta).floatValue())).isFalse();
        assertThat(floatComparer.equalTo(BigDecimal.valueOf(1).floatValue(),
                BigDecimal.valueOf(2).floatValue())).isFalse();
    }

    @Test
    public void testGreaterOrEqualTrue() {
        BigDecimal value = BigDecimal.valueOf(0.5);
        BigDecimal delta = BigDecimal.valueOf(0.00009);

        // Test for equality
        assertThat(floatComparer.greaterOrEqual(value.floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.greaterOrEqual(value.add(delta).floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.greaterOrEqual(value.floatValue(), value.add(delta).floatValue())).isTrue();
        assertThat(floatComparer.greaterOrEqual(value.subtract(delta).floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.greaterOrEqual(value.floatValue(), value.subtract(delta).floatValue())).isTrue();

        // Test greater than
        assertThat(floatComparer.greaterOrEqual(BigDecimal.valueOf(2).floatValue(),
                BigDecimal.valueOf(1).floatValue())).isTrue();
        assertThat(floatComparer.greaterOrEqual(BigDecimal.valueOf(0.55).floatValue(),
                BigDecimal.valueOf(0.5).floatValue())).isTrue();
    }

    @Test
    public void testGreaterOrEqualFalse() {
        assertThat(floatComparer.greaterOrEqual(BigDecimal.valueOf(1).floatValue(),
                BigDecimal.valueOf(2).floatValue())).isFalse();
        assertThat(floatComparer.greaterOrEqual(BigDecimal.valueOf(0.5).floatValue(),
                BigDecimal.valueOf(0.55).floatValue())).isFalse();
    }

    @Test
    public void testLessOrEqualTrue() {
        BigDecimal value = BigDecimal.valueOf(0.5);
        BigDecimal delta = BigDecimal.valueOf(0.00009);

        // Test for equality
        assertThat(floatComparer.lessOrEqual(value.floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.lessOrEqual(value.add(delta).floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.lessOrEqual(value.floatValue(), value.add(delta).floatValue())).isTrue();
        assertThat(floatComparer.lessOrEqual(value.subtract(delta).floatValue(), value.floatValue())).isTrue();
        assertThat(floatComparer.lessOrEqual(value.floatValue(), value.subtract(delta).floatValue())).isTrue();

        // Test less than
        assertThat(floatComparer.lessOrEqual(BigDecimal.valueOf(1).floatValue(),
                BigDecimal.valueOf(2).floatValue())).isTrue();
        assertThat(floatComparer.lessOrEqual(BigDecimal.valueOf(0.5).floatValue(),
                BigDecimal.valueOf(0.55).floatValue())).isTrue();
    }

    @Test
    public void testLessOrEqualFalse() {
        assertThat(floatComparer.lessOrEqual(BigDecimal.valueOf(2).floatValue(),
                BigDecimal.valueOf(1).floatValue())).isFalse();
        assertThat(floatComparer.lessOrEqual(BigDecimal.valueOf(0.55).floatValue(),
                BigDecimal.valueOf(0.5).floatValue())).isFalse();
    }

    @Test
    public void testGreaterThanTrue() {
        assertThat(floatComparer.greaterThan(BigDecimal.valueOf(2).floatValue(),
                BigDecimal.valueOf(1).floatValue())).isTrue();
        assertThat(floatComparer.greaterThan(BigDecimal.valueOf(0.55).floatValue(),
                BigDecimal.valueOf(0.5).floatValue())).isTrue();
        assertThat(floatComparer.greaterThan(BigDecimal.valueOf(0.50011).floatValue(),
                BigDecimal.valueOf(0.5).floatValue())).isTrue();
    }

    @Test
    public void testGreaterThanFalse() {
        BigDecimal value = BigDecimal.valueOf(0.5);
        BigDecimal delta = BigDecimal.valueOf(0.00009);

        // Test for equality
        assertThat(floatComparer.greaterThan(value.floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.greaterThan(value.add(delta).floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.greaterThan(value.floatValue(), value.add(delta).floatValue())).isFalse();
        assertThat(floatComparer.greaterThan(value.subtract(delta).floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.greaterThan(value.floatValue(), value.subtract(delta).floatValue())).isFalse();

        // Test less than
        assertThat(floatComparer.greaterThan(BigDecimal.valueOf(1).floatValue(),
                BigDecimal.valueOf(2).floatValue())).isFalse();
        assertThat(floatComparer.greaterThan(BigDecimal.valueOf(0.5).floatValue(),
                BigDecimal.valueOf(0.55).floatValue())).isFalse();
    }

    @Test
    public void testLessThanTrue() {
        assertThat(floatComparer.lessThan(BigDecimal.valueOf(1).floatValue(),
                BigDecimal.valueOf(2).floatValue())).isTrue();
        assertThat(floatComparer.lessThan(BigDecimal.valueOf(0.5).floatValue(),
                BigDecimal.valueOf(0.55).floatValue())).isTrue();
        assertThat(floatComparer.lessThan(BigDecimal.valueOf(0.5).floatValue(),
                BigDecimal.valueOf(0.50011).floatValue())).isTrue();
    }

    @Test
    public void testLessThanFalse() {
        BigDecimal value = BigDecimal.valueOf(0.5);
        BigDecimal delta = BigDecimal.valueOf(0.00009);

        // Test for equality
        assertThat(floatComparer.lessThan(value.floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.lessThan(value.add(delta).floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.lessThan(value.floatValue(), value.add(delta).floatValue())).isFalse();
        assertThat(floatComparer.lessThan(value.subtract(delta).floatValue(), value.floatValue())).isFalse();
        assertThat(floatComparer.lessThan(value.floatValue(), value.subtract(delta).floatValue())).isFalse();

        // Test less than
        assertThat(floatComparer.lessThan(BigDecimal.valueOf(2).floatValue(),
                BigDecimal.valueOf(1).floatValue())).isFalse();
        assertThat(floatComparer.lessThan(BigDecimal.valueOf(0.55).floatValue(),
                BigDecimal.valueOf(0.5).floatValue())).isFalse();
    }
}
