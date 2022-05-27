package at.ac.tuwien.ba.demo.api.endpoint.v1.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ImageType {
    TCI("tci"),
    NDVI("ndvi");

    private final String name;

    ImageType(String name) {
        this.name = name;
    }

    @JsonCreator
    public static ImageType fromString(String name){
        return ImageType.valueOf(name.toUpperCase());
    }
}
