package com.github.hexabid.core.auctioning.port.in;

import com.github.hexabid.core.auctioning.model.DocumentStatus;
import com.github.hexabid.core.auctioning.model.DocumentType;

public sealed interface SubmitDocumentResult permits SubmitDocumentResult.DocumentAccepted, SubmitDocumentResult.DocumentRejected {

    record DocumentAccepted(DocumentType type, DocumentStatus status) implements SubmitDocumentResult { }

    record DocumentRejected(String reason) implements SubmitDocumentResult { }
}
