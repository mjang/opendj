/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2010 Sun Microsystems, Inc.
 * Portions copyright 2012-2014 ForgeRock AS.
 */
package org.forgerock.opendj.ldap.requests;

import java.util.Collection;

import org.forgerock.opendj.ldap.Attribute;
import org.forgerock.opendj.ldap.AttributeDescription;
import org.forgerock.opendj.ldap.AttributeParser;
import org.forgerock.opendj.ldap.Attributes;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.DN;

import org.forgerock.opendj.ldif.ChangeRecordVisitor;
import org.forgerock.util.Function;
import org.forgerock.util.promise.NeverThrowsException;
import com.forgerock.opendj.util.Iterables;

/**
 * Unmodifiable add request implementation.
 */
final class UnmodifiableAddRequestImpl extends AbstractUnmodifiableRequest<AddRequest> implements
        AddRequest {
    private static final Function<Attribute, Attribute, NeverThrowsException> UNMODIFIABLE_ATTRIBUTE_FUNCTION =
            new Function<Attribute, Attribute, NeverThrowsException>() {
                @Override
                public Attribute apply(final Attribute value) {
                    return Attributes.unmodifiableAttribute(value);
                }
            };

    UnmodifiableAddRequestImpl(final AddRequest impl) {
        super(impl);
    }

    @Override
    public <R, P> R accept(final ChangeRecordVisitor<R, P> v, final P p) {
        return v.visitChangeRecord(p, this);
    }

    @Override
    public boolean addAttribute(final Attribute attribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAttribute(final Attribute attribute,
            final Collection<? super ByteString> duplicateValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddRequest addAttribute(final String attributeDescription, final Object... values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddRequest clearAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAttribute(final Attribute attribute,
            final Collection<? super ByteString> missingValues) {
        return impl.containsAttribute(attribute, missingValues);
    }

    @Override
    public boolean containsAttribute(final String attributeDescription, final Object... values) {
        return impl.containsAttribute(attributeDescription, values);
    }

    @Override
    public Iterable<Attribute> getAllAttributes() {
        return Iterables.unmodifiableIterable(Iterables.transformedIterable(
                impl.getAllAttributes(), UNMODIFIABLE_ATTRIBUTE_FUNCTION));
    }

    @Override
    public Iterable<Attribute> getAllAttributes(final AttributeDescription attributeDescription) {
        return Iterables.unmodifiableIterable(Iterables.transformedIterable(impl
                .getAllAttributes(attributeDescription), UNMODIFIABLE_ATTRIBUTE_FUNCTION));
    }

    @Override
    public Iterable<Attribute> getAllAttributes(final String attributeDescription) {
        return Iterables.unmodifiableIterable(Iterables.transformedIterable(impl
                .getAllAttributes(attributeDescription), UNMODIFIABLE_ATTRIBUTE_FUNCTION));
    }

    @Override
    public Attribute getAttribute(final AttributeDescription attributeDescription) {
        final Attribute attribute = impl.getAttribute(attributeDescription);
        if (attribute != null) {
            return Attributes.unmodifiableAttribute(attribute);
        } else {
            return null;
        }
    }

    @Override
    public Attribute getAttribute(final String attributeDescription) {
        final Attribute attribute = impl.getAttribute(attributeDescription);
        if (attribute != null) {
            return Attributes.unmodifiableAttribute(attribute);
        } else {
            return null;
        }
    }

    @Override
    public int getAttributeCount() {
        return impl.getAttributeCount();
    }

    @Override
    public DN getName() {
        return impl.getName();
    }

    @Override
    public AttributeParser parseAttribute(final AttributeDescription attributeDescription) {
        return impl.parseAttribute(attributeDescription);
    }

    @Override
    public AttributeParser parseAttribute(final String attributeDescription) {
        return impl.parseAttribute(attributeDescription);
    }

    @Override
    public boolean removeAttribute(final Attribute attribute,
            final Collection<? super ByteString> missingValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAttribute(final AttributeDescription attributeDescription) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddRequest removeAttribute(final String attributeDescription, final Object... values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replaceAttribute(final Attribute attribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddRequest replaceAttribute(final String attributeDescription, final Object... values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddRequest setName(final DN dn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AddRequest setName(final String dn) {
        throw new UnsupportedOperationException();
    }
}
