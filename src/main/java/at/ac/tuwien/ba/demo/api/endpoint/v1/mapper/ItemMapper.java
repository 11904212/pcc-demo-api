package at.ac.tuwien.ba.demo.api.endpoint.v1.mapper;


import at.ac.tuwien.ba.demo.api.endpoint.v1.dto.ItemInfoDto;
import at.ac.tuwien.ba.stac.client.core.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemInfoDto itemToDto(Item item) {
        var dto = new ItemInfoDto();
        item.getCollection().ifPresent(dto::setCollectionId);
        item.getDateTime().ifPresent(dto::setDateTime);
        dto.setId(item.getId());
        return dto;
    }
}
