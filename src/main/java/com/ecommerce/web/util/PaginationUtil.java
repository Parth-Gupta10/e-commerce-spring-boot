package com.ecommerce.web.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static Pageable createPageable(Integer pageNumber, Integer pageSize, String sortingField, String sortingDirection) {
        if (sortingField != null) {
            Sort sortDetails = Sort.by(Sort.Direction.fromString(sortingDirection), sortingField);
            return PageRequest.of(pageNumber, pageSize, sortDetails);
        } else {
            return PageRequest.of(pageNumber, pageSize);
        }
    }
} 