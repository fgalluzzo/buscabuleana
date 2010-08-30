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
		public Float getWeight() {
			return weight;
		}
		public void setWeight(Float weight) {
			this.weight = weight;
		}
		
		@Override
		public String toString() {
			return String.format("<%s,%s,%s>", nome, weight, backgroundColor);
		}
	}
	
	public static class RowData {
		private ColumnData firstColumn;
		private List<ColumnData> otherColumns;
		
		public ColumnData getFirstColumn() {
			return firstColumn;
		}
		public void setFirstColumn(ColumnData firstColumn) {
			this.firstColumn = firstColumn;
		}
		public List<ColumnData> getOtherColumns() {
			return otherColumns;
		}
		public void setOtherColumns(List<ColumnData> otherColumns) {
			this.otherColumns = otherColumns;
		}
		
		@Override
		public String toString() {
			return String.format("(%s,%s)", firstColumn.toString(), otherColumns.toString());
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
