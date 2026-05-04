package com.github.hexabid.core.auctioning.usecase;

import com.github.hexabid.core.auctioning.model.DocumentStatus;
import com.github.hexabid.core.auctioning.model.DocumentType;
import com.github.hexabid.core.auctioning.port.in.SubmitDocumentCommand;
import com.github.hexabid.core.auctioning.port.in.SubmitDocumentResult;
import com.github.hexabid.core.auctioning.port.in.SubmitDocumentUseCase;
import com.github.hexabid.core.auctioning.port.out.DocumentRepository;

import java.util.Objects;

public final class SubmitDocumentService implements SubmitDocumentUseCase {

    private final DocumentRepository documentRepository;

    public SubmitDocumentService(DocumentRepository documentRepository) {
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository must not be null");
    }

    @Override
    public SubmitDocumentResult submitDocument(SubmitDocumentCommand command) {
        if (command.status() == DocumentStatus.MISSING) {
            return new SubmitDocumentResult.DocumentRejected("cannot submit a document with MISSING status");
        }
        documentRepository.submitDocument(command.partyId(), command.documentType(), command.status());
        return new SubmitDocumentResult.DocumentAccepted(command.documentType(), command.status());
    }
}
