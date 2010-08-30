package DTO;

import java.util.List;

import bean.MedicamentoBean;

public class TabelaDTO {
	
	// entrada
	private String sintomas;
	private String doencas;
	private String alergenicos;
	private String farmacos;
	
	
	public static class ColumnData {
		private String backgroundColor;
		private Float weight;
		private String nome;
		public String getBackgroundColor() {
			return backgroundColor;
		}
		public void setBackgroundColor(String backgroundColor) {
			this.backgroundColor = backgroundColor;
		}
		public String getNome() {
			return nome;
		}
		public void setNome(String nome) {
			this.nome = nome;
		}
	}
	
	public static class RowData {
		ColumnData firstColumn;
		ColumnData [] otherColumn;
		public ColumnData getFirstColumn() {
			return firstColumn;
		}
		public void setFirstColumn(ColumnData firstColumn) {
			this.firstColumn = firstColumn;
		}
		public ColumnData[] getOtherColumn() {
			return otherColumn;
		}
		public void setOtherColumn(ColumnData[] otherColumn) {
			this.otherColumn = otherColumn;
		}
	}
	
	// saida
	private List <RowData> results;	
	private List <MedicamentoBean> medicamentos;

	
	
	
	
	public String getSintomas() {
		return sintomas;
	}

	public void setSintomas(String sintomas) {
		this.sintomas = sintomas;
	}

	public String getDoencas() {
		return doencas;
	}

	public void setDoencas(String doencas) {
		this.doencas = doencas;
	}

	public String getAlergenicos() {
		return alergenicos;
	}

	public void setAlergenicos(String alergenicos) {
		this.alergenicos = alergenicos;
	}
	
	public String getFarmacos() {
		return farmacos;
	}
	
	public void setFarmacos(String farmacos) {
		this.farmacos = farmacos;
	}

	public List<RowData> getResults() {
		return results;
	}

	public void setResults(List<RowData> results) {
		this.results = results;
	}

	public List<MedicamentoBean> getMedicamentos() {
		return medicamentos;
	}

	public void setMedicamentos(List<MedicamentoBean> medicamentos) {
		this.medicamentos = medicamentos;
	}
	
	
	
}
