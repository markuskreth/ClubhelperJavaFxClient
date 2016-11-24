package de.kreth.clubhelperclient.reports.factories;

import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSourceProvider;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class GenericBeanDataSourceProvider<T> extends JRAbstractBeanDataSourceProvider {

	private List<T> data = new ArrayList<>();

	public GenericBeanDataSourceProvider(Class<T> beanClass, List<T> data) {
		super(beanClass);
		this.data = data;
	}

	@Override
	public JRDataSource create(JasperReport report) throws JRException {
		return new JRBeanCollectionDataSource(data);
	}

	@Override
	public void dispose(JRDataSource dataSource) throws JRException {
		data.clear();
	}

}
