package integration.model;

import java.util.UUID;

import javax.persistence.PersistenceException;
import javax.persistence.PrePersist;

public class IdListener {

	@PrePersist
	public void prepareId(AbstractAsset asset) {
		Integer id = asset.getId();
		if (id == null || id == 0) {
			if (asset.getSourceId() == null) {
				throw new PersistenceException("invalid ids");
			}
			asset.setId(AbstractAsset.convertToBigInteger(UUID.fromString(asset.getSourceId())).intValue());
		}
	}
}
