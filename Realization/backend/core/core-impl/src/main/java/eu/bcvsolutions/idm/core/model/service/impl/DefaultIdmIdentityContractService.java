package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;

@Service
public class DefaultIdmIdentityContractService extends AbstractReadWriteEntityService<IdmIdentityContract, EmptyFilter> implements IdmIdentityContractService {

	private final IdmIdentityContractRepository repository;
	
	@Autowired
	public DefaultIdmIdentityContractService(IdmIdentityContractRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	public List<IdmIdentityContract> getContracts(IdmIdentity identity) {
		return repository.findAllByIdentity(identity, null);
	}
}
