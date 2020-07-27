package org.uniprot.store.datastore.member.uniref;

import java.util.Objects;

import org.springframework.batch.item.ItemProcessor;
import org.uniprot.core.uniref.RepresentativeMember;
import org.uniprot.core.uniref.UniRefMember;
import org.uniprot.core.uniref.UniRefMemberIdType;
import org.uniprot.core.uniref.impl.RepresentativeMemberBuilder;
import org.uniprot.core.xml.jaxb.uniref.MemberType;
import org.uniprot.core.xml.uniref.MemberConverter;
import org.uniprot.core.xml.uniref.RepresentativeMemberConverter;

/**
 * @author sahmad
 * @since 23/07/2020
 */
public class UniRef100MemberProcessor implements ItemProcessor<MemberType, RepresentativeMember> {
    private final RepresentativeMemberConverter repMemberConverter;
    private final MemberConverter memberConverter;

    public UniRef100MemberProcessor() {
        repMemberConverter = new RepresentativeMemberConverter();
        memberConverter = new MemberConverter();
    }

    @Override
    public RepresentativeMember process(MemberType memberType) throws Exception {
        RepresentativeMemberBuilder builder;

        if (Objects.nonNull(memberType.getSequence())) {
            RepresentativeMember repMember = repMemberConverter.fromXml(memberType);
            builder = RepresentativeMemberBuilder.from(repMember).memberId(getMemberId(repMember));
        } else {
            UniRefMember member = memberConverter.fromXml(memberType);
            builder = RepresentativeMemberBuilder.from(member).memberId(getMemberId(member));
        }
        // update seed to null
        return builder.isSeed(null).build();
    }

    // get accession id if memberType is UniRefMemberIdType.UNIPROTKB
    private String getMemberId(UniRefMember member) {
        if (member.getMemberIdType() == UniRefMemberIdType.UNIPARC) {
            return member.getMemberId();
        } else {
            return member.getUniProtAccessions().get(0).getValue();
        }
    }
}
