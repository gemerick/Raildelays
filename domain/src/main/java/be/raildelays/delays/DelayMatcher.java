package be.raildelays.delays;

import java.time.LocalTime;

/**
 * @author Almex
 * @since 2.0
 */
public interface DelayMatcher<T> extends Matcher<T> {

    static OperatorMatcher<Long> is(OperatorMatcher<Long> matcher) {
        return matcher;
    }

    static OperatorMatcher<Long> is(Long value) {
        return is(equalsTo(value));
    }

    static OperatorMatcher<Long> equalsTo(Long value) {
        return OperatorMatcher.operator(Operator.EQUAL, ValueMatcher.value(value));
    }

    static OperatorMatcher<Long> zero() {
        return OperatorMatcher.operator(Operator.EQUAL, ValueMatcher.value(0L));
    }

    static OperatorMatcher<Long> greaterThan(Long value) {
        return OperatorMatcher.operator(Operator.GREATER, ValueMatcher.value(value));
    }

    static OperatorMatcher<Long> greaterThanOrEqual(Long value) {
        return OperatorMatcher.operator(Operator.GREATER_OR_EQUAL, ValueMatcher.value(value));
    }

    static OperatorMatcher<Long> after() {
        return greaterThan(0L);
    }

    static OperatorMatcher<Long> lessThan(Long value) {
        return OperatorMatcher.operator(Operator.LESS, ValueMatcher.value(value));
    }
    static OperatorMatcher<Long> lessThanOrEqual(Long value) {
        return OperatorMatcher.operator(Operator.LESS_OR_EQUAL, ValueMatcher.value(value));
    }

    static OperatorMatcher<Long> before() {
        return lessThan(0L);
    }

    static boolean difference(OrderingComparison comparison,
                                     OperatorMatcher<Long> matcher) {
        comparison.setOperator(matcher.getOperator());

        return comparison.match(matcher.getValueMatcher().getValue());
    }

    static boolean duration(OrderingComparison comparison,
                                   OperatorMatcher<Long> matcher) {
        // A duration is the opposite of a difference
        return difference(comparison, opposite(matcher));
    }

    /**
     * Create a {@link OperatorMatcher} containing the {@link ValueMatcher} with an opposite {@code value} and the
     * opposite {@link be.raildelays.delays.DelayMatcher.Operator}.
     *
     * @param matcher the {@link OperatorMatcher} to clone
     * @return a {@link OperatorMatcher} containing the {@link ValueMatcher} with an opposite {@code value} and the
     * opposite {@link be.raildelays.delays.DelayMatcher.Operator}.
     * @throws UnsupportedOperationException if the {@link OperatorMatcher#operator} is not supported by this
     * implementation.
     */
    static OperatorMatcher<Long> opposite(OperatorMatcher<Long> matcher) {
        Operator operator = matcher.getOperator();
        ValueMatcher<Long> valueMatcher = ValueMatcher.value(-matcher.getValueMatcher().getValue());

        switch (operator) {
            case GREATER:
                operator = Operator.LESS;
                break;
            case LESS:
                operator = Operator.GREATER;
                break;
            case GREATER_OR_EQUAL:
                operator = Operator.LESS_OR_EQUAL;
                break;
            case LESS_OR_EQUAL:
                operator = Operator.GREATER_OR_EQUAL;
                break;
            case EQUAL:
                operator = Operator.EQUAL;
                break;
            default:
                throw new UnsupportedOperationException(String.format("The '%s' operator is not supported", operator));
        }

        return OperatorMatcher.operator(operator, valueMatcher);
    }

    static OrderingComparison between(TimeDelay from) {
        return new OrderingComparison(from);
    }

    static OrderingComparison between(LocalTime from) {
        return new OrderingComparison(TimeDelay.of(from));
    }

    @Override
    default boolean match(T object) {
        return false;
    }

    enum Operator {
        GREATER, LESS, EQUAL, GREATER_OR_EQUAL, LESS_OR_EQUAL
    }

    class OrderingComparison implements Matcher<Long> {

        private TimeDelay from;
        private TimeDelay to;
        private Operator operator;

        protected OrderingComparison(TimeDelay from) {
            this.from = from;
        }

        @Override
        public boolean match(Long value) {
            boolean result;

            switch (operator) {
                case GREATER:
                    result = Delays.compareTimeAndDelay(from, to) > value;
                    break;
                case GREATER_OR_EQUAL:
                    result = Delays.compareTimeAndDelay(from, to) >= value;
                    break;
                case LESS:
                    result = Delays.compareTimeAndDelay(from, to) < value;
                    break;
                case LESS_OR_EQUAL:
                    result = Delays.compareTimeAndDelay(from, to) <= value;
                    break;
                case EQUAL:
                default:
                    result = Delays.compareTimeAndDelay(from, to) == value;
            }

            return result;
        }

        protected void setTo(TimeDelay to) {
            this.to = to;
        }

        protected void setOperator(Operator operator) {
            this.operator = operator;
        }

        public OrderingComparison and(TimeDelay to) {
            this.setTo(to);

            return this;
        }

        public OrderingComparison and(LocalTime to) {
            this.setTo(TimeDelay.of(to));

            return this;
        }
    }

    class ValueMatcher<V> implements Matcher<V> {

        private V value;

        private ValueMatcher(V value) {
            this.value = value;
        }

        public static <V> ValueMatcher<V> value(V value) {
            return new ValueMatcher<>(value);
        }

        @Override
        public boolean match(V target) {
            boolean result = false;

            if (value != null) {
                result = value.equals(target);
            } else if (target == null) {
                result = true;
            }

            return result;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    class OperatorMatcher<V> implements Matcher<Operator> {

        private Operator operator;
        private ValueMatcher<V> valueMatcher;

        private OperatorMatcher(Operator operator, ValueMatcher<V> valueMatcher) {
            this.operator = operator;
            this.valueMatcher = valueMatcher;
        }

        public static <V> OperatorMatcher<V> operator(Operator operator, ValueMatcher<V> matcher) {
            return new OperatorMatcher<>(operator, matcher);
        }

        @Override
        public boolean match(Operator target) {
            return operator.equals(target);
        }

        public ValueMatcher<V> getValueMatcher() {
            return valueMatcher;
        }

        public Operator getOperator() {
            return operator;
        }
    }
}
