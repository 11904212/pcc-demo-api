package at.ac.tuwien.ba.demo.api.endpoint.v1.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ItemInfoDto {

    private String id;
    private String dateTime;
    private String collectionId;

}
