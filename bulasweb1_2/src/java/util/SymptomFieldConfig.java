package util;

import java.util.List;

public class SymptomFieldConfig {
	
	public static class Field {
		private String name;
		private Float weight;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Float getWeight() {
			return weight;
		}
		public void setWeight(Float weight) {
			this.weight = weight;
		}
	}
	
	public static class Input {
		private String nome;
		private List <Field> fields;
		public String getNome() {
			return nome;
		}
		public void setNome(String nome) {
			this.nome = nome;
		}
		public List<Field> getFields() {
			return fields;
		}
		public void setFields(List<Field> fields) {
			this.fields = fields;
		}
	}
	
	private List <Input> inputs;

	public List<Input> getInputs() {
		return inputs;
	}

	public void setInputs(List<Input> inputs) {
		this.inputs = inputs;
	}
}
