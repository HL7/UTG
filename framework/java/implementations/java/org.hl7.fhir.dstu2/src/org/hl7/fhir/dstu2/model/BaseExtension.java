package org.hl7.fhir.dstu2.model;

import org.hl7.fhir.dstu2.model.api.IBaseDatatype;
import org.hl7.fhir.dstu2.model.api.IBaseExtension;
import org.hl7.fhir.dstu2.model.api.IBaseHasExtensions;

public abstract class BaseExtension extends Type implements IBaseExtension<Extension, Type>, IBaseHasExtensions {

	@Override
	public Extension setValue(IBaseDatatype theValue) {
		setValue((Type)theValue);
		return (Extension) this;
	}
	
	public abstract Extension setValue(Type theValue);
	
}
