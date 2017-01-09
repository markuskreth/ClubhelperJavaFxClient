package de.kreth.clubhelperclient.reports.factories;

import java.util.List;

import de.kreth.clubhelperclient.reports.data.PhoneListPerson;

public class PhoneListDataSrourceProvider extends GenericBeanDataSourceProvider<PhoneListPerson> {

	public PhoneListDataSrourceProvider(Class<PhoneListPerson> beanClass, List<PhoneListPerson> data) {
		super(beanClass, data);
	}

}
