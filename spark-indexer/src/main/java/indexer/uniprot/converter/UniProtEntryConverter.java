package indexer.uniprot.converter;

import lombok.extern.slf4j.Slf4j;
import org.uniprot.core.DBCrossReference;
import org.uniprot.core.Sequence;
import org.uniprot.core.cv.keyword.KeywordCategory;
import org.uniprot.core.gene.Gene;
import org.uniprot.core.scorer.uniprotkb.UniProtEntryScored;
import org.uniprot.core.uniprot.*;
import org.uniprot.core.uniprot.evidence.Evidence;
import org.uniprot.core.uniprot.evidence.EvidenceTypeCategory;
import org.uniprot.core.util.Utils;
import org.uniprot.store.indexer.util.DateUtils;
import org.uniprot.store.job.common.DocumentConversionException;
import org.uniprot.store.job.common.converter.DocumentConverter;
import org.uniprot.store.search.document.uniprot.UniProtDocument;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static indexer.uniprot.converter.UniProtEntryConverterUtil.*;

/**
 * Created 18/04/19
 *
 * @author Edd
 */
@Slf4j
public class UniProtEntryConverter implements DocumentConverter<UniProtEntry, UniProtDocument>, Serializable {

    private static final long serialVersionUID = -4786571927033506456L;
    private static final String DASH = "-";
    /**
     * An enum set representing all of the organelles that are children of plastid
     */
    private static final EnumSet<GeneEncodingType> PLASTID_CHILD = EnumSet.of(GeneEncodingType.APICOPLAST,
            GeneEncodingType.CHLOROPLAST, GeneEncodingType.CYANELLE,
            GeneEncodingType.NON_PHOTOSYNTHETIC_PLASTID, GeneEncodingType.ORGANELLAR_CHROMATOPHORE);

    private final UniProtEntryCommentsConverter commentsConverter;
    private final UniProtEntryFeatureConverter featureConverter;
    private final UniProtEntryReferencesConverter referencesConverter;
    private final UniProtEntryCrossReferenceConverter crossReferenceConverter;
    private final UniprotEntryTaxonomyConverter taxonomyConverter;
    private final UniprotEntryProteinDescriptionConverter proteinDescriptionConverter;

    public UniProtEntryConverter(Map<String, String> pathway) {
        this.taxonomyConverter = new UniprotEntryTaxonomyConverter();
        this.crossReferenceConverter = new UniProtEntryCrossReferenceConverter();
        this.commentsConverter = new UniProtEntryCommentsConverter(pathway);
        this.featureConverter = new UniProtEntryFeatureConverter();
        this.referencesConverter = new UniProtEntryReferencesConverter();
        this.proteinDescriptionConverter = new UniprotEntryProteinDescriptionConverter();
    }

    @Override
    public UniProtDocument convert(UniProtEntry source) {
        try {
            UniProtDocument document = new UniProtDocument();

            document.accession = source.getPrimaryAccession().getValue();
            if (document.accession.contains(DASH)) {
                if (isCanonicalIsoform(source)) {
                    document.reviewed = null;
                    document.isIsoform = null;
                    return document;
                }
                document.isIsoform = true;
                // We are adding the canonical accession to the isoform entry as a secondary
                // accession.
                // this way when you search by an accession and ask to include isoforms, it will
                // find it.
                String canonicalAccession = document.accession.substring(0, document.accession.indexOf(DASH));
                document.secacc.add(canonicalAccession);
            } else {
                document.isIsoform = false;
            }
            document.reviewed = (source.getEntryType() == UniProtEntryType.SWISSPROT);
            addValueListToStringList(document.secacc, source.getSecondaryAccessions());
            document.content.add(document.accession);
            document.content.addAll(document.secacc);

            proteinDescriptionConverter.convertProteinDescription(source.getProteinDescription(), document);
            taxonomyConverter.convertOrganism(source.getOrganism(), document);
            taxonomyConverter.convertOrganismHosts(source.getOrganismHosts(), document);
            referencesConverter.convertReferences(source.getReferences(), document);
            commentsConverter.convertCommentToDocument(source.getComments(), document);
            crossReferenceConverter.convertCrossReferences(source.getDatabaseCrossReferences(), document);
            featureConverter.convertFeature(source.getFeatures(), document);
            convertUniprotId(source.getUniProtId(), document);
            convertEntryAudit(source.getEntryAudit(), document);
            convertGeneNames(source.getGenes(), document);
            convertKeywords(source.getKeywords(), document);
            convertOrganelle(source.getGeneLocations(), document);
            convertProteinExistence(source.getProteinExistence(), document);
            convertSequence(source.getSequence(), document);
            convertEntryScore(source, document);
            convertEvidenceSources(source, document);
            convertUniRefClusters(document.accession, document);

            return document;
        } catch (Exception e) {
            String message = "Error converting UniProt entry";
            if (source != null && source.getPrimaryAccession() != null) {
                message += ": " + source.getPrimaryAccession().getValue();
            }
            log.error(message, e);
            throw new DocumentConversionException(message, e);
        }
    }

    private void convertEntryAudit(EntryAudit entryAudit, UniProtDocument document) {
        if (entryAudit != null) {
            document.firstCreated = DateUtils.convertLocalDateToDate(entryAudit.getFirstPublicDate());
            document.lastModified = DateUtils.convertLocalDateToDate(entryAudit.getLastAnnotationUpdateDate());
            document.sequenceUpdated = DateUtils.convertLocalDateToDate(entryAudit.getLastSequenceUpdateDate());
        }
    }

    private void convertEvidenceSources(UniProtEntry uniProtEntry, UniProtDocument document) {
        List<Evidence> evidences = uniProtEntry.gatherEvidences();
        document.sources = evidences.stream().map(Evidence::getSource)
                .filter(Objects::nonNull)
                .map(DBCrossReference::getDatabaseType)
                .filter(val -> (val != null) && val.getDetail().getCategory() == EvidenceTypeCategory.A)
                .map(val -> {
                    String data = val.getName();
                    if (data.equalsIgnoreCase("HAMAP-rule"))
                        data = "HAMAP";
                    return data;
                })
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private void convertUniprotId(UniProtId uniProtId, UniProtDocument document) {
        document.id = uniProtId.getValue();
        document.content.add(document.id);
        String[] idParts = document.id.split("_");
        if (idParts.length == 2) {
            if (document.reviewed) {
                // first component of swiss-prot id is gene, which we want searchable in the
                // mnemonic
                document.idDefault = document.id;
            } else {
                // don't add first component for trembl entries, since this is the accession,
                // and
                // we do not want false boosting for default searches that match a substring of
                // the accession
                document.idDefault = idParts[1];
            }
        }
    }

    private void convertUniRefClusters(String accession, UniProtDocument document) {
        // document.unirefCluster50 = uniprotUniRefMap.getMappings50(accession);
        // document.unirefCluster90 = uniprotUniRefMap.getMappings90(accession);
        // document.unirefCluster100 = uniprotUniRefMap.getMappings100(accession);
    }

    private void convertEntryScore(UniProtEntry source, UniProtDocument document) {
        UniProtEntryScored entryScored = new UniProtEntryScored(source);
        double score = entryScored.score();
        int q = (int) (score / 20d);
        document.score = q > 4 ? 5 : q + 1;
    }

    private void convertSequence(Sequence seq, UniProtDocument document) {
        document.seqLength = seq.getLength();
        document.seqMass = seq.getMolWeight();
    }

    private void convertKeywords(List<Keyword> keywords, UniProtDocument document) {
        if (Utils.notNullOrEmpty(keywords)) {
            keywords.forEach(keyword -> updateKeyword(keyword, document));
            document.content.addAll(document.keywords);
        }
    }

    private void updateKeyword(Keyword keyword, UniProtDocument document) {
        document.keywords.add(keyword.getId());
        addValueToStringList(document.keywords, keyword);
        KeywordCategory kc = keyword.getCategory();
        if (!document.keywords.contains(kc.getAccession())) {
            document.keywords.add(kc.getAccession());
            document.keywords.add(kc.getName());
        }
    }

    private void convertGeneNames(List<Gene> genes, UniProtDocument document) {
        if (Utils.notNullOrEmpty(genes)) {
            for (Gene gene : genes) {
                addValueToStringList(document.geneNamesExact, gene.getGeneName());
                addValueListToStringList(document.geneNamesExact, gene.getSynonyms());
                addValueListToStringList(document.geneNamesExact, gene.getOrderedLocusNames());
                addValueListToStringList(document.geneNamesExact, gene.getOrfNames());
            }
            document.geneNames.addAll(document.geneNamesExact);
            document.content.addAll(document.geneNamesExact);
            document.geneNamesSort = truncatedSortValue(String.join(" ", document.geneNames));
        }
    }

    private void convertOrganelle(List<GeneLocation> geneLocations, UniProtDocument document) {
        if (Utils.notNullOrEmpty(geneLocations)) {
            for (GeneLocation geneLocation : geneLocations) {
                GeneEncodingType geneEncodingType = geneLocation.getGeneEncodingType();

                if (PLASTID_CHILD.contains(geneEncodingType)) {
                    document.organelles.add(GeneEncodingType.PLASTID.getName().toLowerCase());
                }
                document.organelles.add(geneEncodingType.getName().toLowerCase());
            }
            document.content.addAll(document.organelles);
        }
    }

    private void convertProteinExistence(ProteinExistence proteinExistence, UniProtDocument document) {
        if (proteinExistence != null) {
            document.proteinExistence = proteinExistence.name();
        }
    }
}