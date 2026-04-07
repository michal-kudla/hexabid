package com.acme.auctions.product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectionRuleTest {

    @Test
    void shouldSatisfySingleRule() {
        ProductSet set = ProductSet.singleOf("Laptop", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.single(set);

        List<SelectedProduct> selection = List.of(new SelectedProduct(set.products().iterator().next(), 1));

        assertTrue(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldFailSingleRuleWhenZeroSelected() {
        ProductSet set = ProductSet.singleOf("Laptop", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.single(set);

        assertFalse(rule.isSatisfiedBy(List.of()));
    }

    @Test
    void shouldSatisfyOptionalRuleWhenNoneSelected() {
        ProductSet set = ProductSet.singleOf("Mouse", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.optional(set);

        assertTrue(rule.isSatisfiedBy(List.of()));
    }

    @Test
    void shouldSatisfyOptionalRuleWhenOneSelected() {
        ProductSet set = ProductSet.singleOf("Mouse", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.optional(set);

        List<SelectedProduct> selection = List.of(new SelectedProduct(set.products().iterator().next(), 1));

        assertTrue(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldFailOptionalRuleWhenTwoSelected() {
        ProductSet set = ProductSet.of("Accessories", ProductIdentifier.randomUuid(), ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.optional(set);

        List<SelectedProduct> selection = set.products().stream()
            .map(id -> new SelectedProduct(id, 1))
            .toList();

        assertFalse(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldSatisfyRequiredRule() {
        ProductSet set = ProductSet.singleOf("Laptop", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.required(set);

        List<SelectedProduct> selection = List.of(new SelectedProduct(set.products().iterator().next(), 1));

        assertTrue(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldFailRequiredRuleWhenNoneSelected() {
        ProductSet set = ProductSet.singleOf("Laptop", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.required(set);

        assertFalse(rule.isSatisfiedBy(List.of()));
    }

    @Test
    void shouldSatisfyAndRuleWhenAllSatisfied() {
        ProductSet laptops = ProductSet.singleOf("Laptop", ProductIdentifier.randomUuid());
        ProductSet bags = ProductSet.singleOf("Bag", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.and(
            SelectionRule.single(laptops),
            SelectionRule.optional(bags)
        );

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(laptops.products().iterator().next(), 1),
            new SelectedProduct(bags.products().iterator().next(), 1)
        );

        assertTrue(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldFailAndRuleWhenOneNotSatisfied() {
        ProductSet laptops = ProductSet.singleOf("Laptop", ProductIdentifier.randomUuid());
        ProductSet bags = ProductSet.singleOf("Bag", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.and(
            SelectionRule.single(laptops),
            SelectionRule.single(bags)
        );

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(laptops.products().iterator().next(), 1)
        );

        assertFalse(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldSatisfyOrRuleWhenOneSatisfied() {
        ProductSet windows = ProductSet.singleOf("Windows", ProductIdentifier.randomUuid());
        ProductSet macos = ProductSet.singleOf("macOS", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.or(
            SelectionRule.single(windows),
            SelectionRule.single(macos)
        );

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(windows.products().iterator().next(), 1)
        );

        assertTrue(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldFailOrRuleWhenNoneSatisfied() {
        ProductSet windows = ProductSet.singleOf("Windows", ProductIdentifier.randomUuid());
        ProductSet macos = ProductSet.singleOf("macOS", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.or(
            SelectionRule.single(windows),
            SelectionRule.single(macos)
        );

        assertFalse(rule.isSatisfiedBy(List.of()));
    }

    @Test
    void shouldSatisfyNotRule() {
        ProductSet set = ProductSet.singleOf("Extra", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.not(SelectionRule.required(set));

        assertTrue(rule.isSatisfiedBy(List.of()));
    }

    @Test
    void shouldFailNotRuleWhenInnerSatisfied() {
        ProductSet set = ProductSet.singleOf("Extra", ProductIdentifier.randomUuid());
        SelectionRule rule = SelectionRule.not(SelectionRule.required(set));

        List<SelectedProduct> selection = List.of(new SelectedProduct(set.products().iterator().next(), 1));

        assertFalse(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldSatisfyConditionalRuleWhenConditionTrueAndThenSatisfied() {
        ProductSet gaming = ProductSet.singleOf("Gaming Laptop", ProductIdentifier.randomUuid());
        ProductSet gpu = ProductSet.singleOf("Dedicated GPU", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.ifThen(
            SelectionRule.single(gaming),
            SelectionRule.single(gpu)
        );

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(gaming.products().iterator().next(), 1),
            new SelectedProduct(gpu.products().iterator().next(), 1)
        );

        assertTrue(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldFailConditionalRuleWhenConditionTrueAndThenNotSatisfied() {
        ProductSet gaming = ProductSet.singleOf("Gaming Laptop", ProductIdentifier.randomUuid());
        ProductSet gpu = ProductSet.singleOf("Dedicated GPU", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.ifThen(
            SelectionRule.single(gaming),
            SelectionRule.single(gpu)
        );

        List<SelectedProduct> selection = List.of(
            new SelectedProduct(gaming.products().iterator().next(), 1)
        );

        assertFalse(rule.isSatisfiedBy(selection));
    }

    @Test
    void shouldPassConditionalRuleWhenConditionFalse() {
        ProductSet gaming = ProductSet.singleOf("Gaming Laptop", ProductIdentifier.randomUuid());
        ProductSet gpu = ProductSet.singleOf("Dedicated GPU", ProductIdentifier.randomUuid());

        SelectionRule rule = SelectionRule.ifThen(
            SelectionRule.single(gaming),
            SelectionRule.single(gpu)
        );

        assertTrue(rule.isSatisfiedBy(List.of()));
    }
}
