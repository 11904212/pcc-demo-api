package at.ac.tuwien.ba.demo.api.endpoint.v1.dto;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Setter
public class ItemInfoDto {

    private String id;
    private LocalDate date;
    private String collection;
    private List<String> bands;

}
