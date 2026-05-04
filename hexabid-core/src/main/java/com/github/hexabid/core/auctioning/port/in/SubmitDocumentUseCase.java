package com.github.hexabid.core.auctioning.port.in;

public interface SubmitDocumentUseCase {
    SubmitDocumentResult submitDocument(SubmitDocumentCommand command);
}
