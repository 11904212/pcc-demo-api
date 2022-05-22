package at.ac.tuwien.ba.demo.api.endpoint.v1.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ItemInfoDto {

    private String id;
    private ZonedDateTime date;
    private String collection;

}
