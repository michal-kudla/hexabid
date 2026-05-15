package com.github.hexabid.authorization.core.scope.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationCodeTest {

    @Test
    void sameCodeIsSameOrBelow() {
        var code = new OrganisationCode("A12/B04");
        assertTrue(code.isSameOrBelow(new OrganisationCode("A12/B04")));
    }

    @Test
    void childCodeIsBelowParent() {
        var parent = new OrganisationCode("A12/B04");
        assertTrue(parent.isSameOrBelow(new OrganisationCode("A12/B04/C77")));
        assertTrue(parent.isSameOrBelow(new OrganisationCode("A12/B04/C77/D01")));
    }

    @Test
    void similarPrefixIsNotBelowParent() {
        var parent = new OrganisationCode("A12/B04");
        assertFalse(parent.isSameOrBelow(new OrganisationCode("A12/B040/C77")));
        assertFalse(parent.isSameOrBelow(new OrganisationCode("A12/B040")));
    }

    @Test
    void parentIsNotBelowChild() {
        var child = new OrganisationCode("A12/B04/C77");
        assertFalse(child.isSameOrBelow(new OrganisationCode("A12/B04")));
    }

    @Test
    void differentBranchIsNotBelow() {
        var code = new OrganisationCode("A12/B04/C77");
        assertFalse(code.isSameOrBelow(new OrganisationCode("A12/B04/C88")));
    }

    @Test
    void rootCodeMatchesAll() {
        var root = new OrganisationCode("A12");
        assertTrue(root.isSameOrBelow(new OrganisationCode("A12/B04")));
        assertTrue(root.isSameOrBelow(new OrganisationCode("A12/B04/C77")));
    }

    @Test
    void rejectsBlankCode() {
        assertThrows(IllegalArgumentException.class, () -> new OrganisationCode(""));
    }

    @Test
    void rejectsDoubleSeparator() {
        assertThrows(IllegalArgumentException.class, () -> new OrganisationCode("A12//B04"));
    }

    @Test
    void rejectsLeadingSeparator() {
        assertThrows(IllegalArgumentException.class, () -> new OrganisationCode("/A12/B04"));
    }

    @Test
    void rejectsTrailingSeparator() {
        assertThrows(IllegalArgumentException.class, () -> new OrganisationCode("A12/B04/"));
    }

    @Test
    void rejectsNullValue() {
        assertThrows(NullPointerException.class, () -> new OrganisationCode(null));
    }

    @Test
    void toStringReturnsValue() {
        assertEquals("A12/B04/C77", new OrganisationCode("A12/B04/C77").toString());
    }
}
