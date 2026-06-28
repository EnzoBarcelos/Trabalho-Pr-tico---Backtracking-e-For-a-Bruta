package modelo;

// Os dois simbolos do jogo (Sol e Lua) mais o marcador de celula vazia.
public enum Simbolo {
    SOL('S'),
    LUA('L'),
    VAZIO('.');

    public final char c; // caractere usado na leitura do arquivo e na impressao

    Simbolo(char c) {
        this.c = c;
    }

    public Simbolo oposto() {
        if (this == SOL) return LUA;
        if (this == LUA) return SOL;
        return VAZIO;
    }

    // Qualquer caractere diferente de S/L vira VAZIO.
    public static Simbolo deChar(char ch) {
        switch (Character.toUpperCase(ch)) {
            case 'S': return SOL;
            case 'L': return LUA;
            default:  return VAZIO;
        }
    }
}
