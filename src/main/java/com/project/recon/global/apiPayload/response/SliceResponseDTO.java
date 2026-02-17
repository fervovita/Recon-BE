package com.project.recon.global.apiPayload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SliceResponseDTO<T> {

    private List<T> content;
    private int page;
    private int size;
    private boolean hasNext;

    public static <T> SliceResponseDTO<T> of(Slice<T> slice) {
        return SliceResponseDTO.<T>builder()
                .content(slice.getContent())
                .page(slice.getNumber() + 1)
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }
}
