package modelo;

// Ligacao entre duas celulas vizinhas:
//   IGUAL  -> mesmo simbolo  (sinal '=')
//   OPOSTO -> simbolos opostos (sinal 'x')
public enum Restricao {
    NENHUMA,
    IGUAL,
    OPOSTO;

    public static Restricao deChar(char ch) {
        if (ch == '=') return IGUAL;
        if (ch == 'x' || ch == 'X') return OPOSTO;
        return NENHUMA;
    }
}
