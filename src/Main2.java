
public class Main2 {

	static int suma(int mn1, int nm2) {
		return (mn1 + nm2);
	}

	static void mostrarPorConsola(int resultado) {
		System.out.println(resultado);
	}

	

	public static void main(String[] args) {

		int num1 = 0;
		int num2 = 3;

		int resultado = suma(num1, num2);

		mostrarPorConsola(resultado);

	}

}