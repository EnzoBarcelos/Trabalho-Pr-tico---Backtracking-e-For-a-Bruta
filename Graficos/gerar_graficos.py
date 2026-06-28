#!/usr/bin/env python3
# Gera os graficos do trabalho a partir dos CSVs produzidos pelo Benchmark Java.
# Uso: python gerar_graficos.py [benchmark_csv] [perfil_csv]
# Defaults: benchmark_tango.csv e perfil_nivel.csv
import csv
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

AZUL = "#1f77b4"   # Backtracking
VERM = "#d62728"   # Forca Bruta


def ler_csv(caminho):
    with open(caminho, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))


def to_float(x):
    try:
        return float(x)
    except (ValueError, TypeError):
        return None


def rotulo(inst, n, k):
    base = inst.replace(".txt", "").replace("_", " ")
    return f"{base}\n(n={n}, K={k})"


def por_instancia(linhas):
    # Agrupa o CSV por instancia, ordenado por numero de celulas vazias (K).
    insts = {}
    for r in linhas:
        nome = r["instancia"]
        k = int(r["vazias"])
        n = int(r["n"])
        d = insts.setdefault(nome, {"nome": nome, "n": n, "k": k})
        est = to_float(r["estados"])
        tmed = to_float(r["tempo_ms_media"])
        tstd = to_float(r["tempo_ms_std"])
        if r["solver"] == "Backtracking":
            d["bt_est"] = est
            d["bt_t"] = tmed
            d["bt_std"] = tstd
        else:
            d["fb_est"] = est
            d["fb_t"] = tmed
            d["fb_std"] = tstd
            d["fb_pulado"] = (r["resolveu"] == "pulado")
    return sorted(insts.values(), key=lambda d: d["k"])


def grafico_estados(insts):
    fig, ax = plt.subplots(figsize=(8, 5))
    ks = [d["k"] for d in insts]

    ax.plot(ks, [d["bt_est"] for d in insts], "o-", color=AZUL, lw=2, ms=7,
            label="Backtracking (medido)")

    med = [d for d in insts if not d.get("fb_pulado")]
    ax.plot([d["k"] for d in med], [d["fb_est"] for d in med], "s-", color=VERM, lw=2, ms=7,
            label="Forca Bruta (medido)")

    teo = [d for d in insts if d.get("fb_pulado")]
    if teo:
        ax.plot([d["k"] for d in teo], [d["fb_est"] for d in teo], "s", color=VERM, ms=10,
                mfc="none", mew=2, label=r"Forca Bruta (limite teorico $2^K$)")

    ax.plot(ks, [2 ** k for k in ks], ":", color="gray", lw=1.5, label=r"Espaco de busca $2^K$")

    ax.set_yscale("log")
    ax.set_xlabel("Celulas vazias (K)")
    ax.set_ylabel("Estados explorados (log)")
    ax.set_title("Estados explorados: Forca Bruta vs Backtracking")
    ax.grid(True, which="both", ls="--", alpha=0.4)
    ax.legend()
    fig.tight_layout()
    fig.savefig("grafico_estados.png", dpi=150)
    plt.close(fig)
    print("grafico_estados.png")


def grafico_tempo(insts):
    fig, ax = plt.subplots(figsize=(8, 5))
    bt = [d for d in insts if d.get("bt_t") is not None]
    ax.errorbar([d["k"] for d in bt], [d["bt_t"] for d in bt], yerr=[d["bt_std"] for d in bt],
                fmt="o-", color=AZUL, lw=2, ms=7, capsize=4, label="Backtracking")
    fb = [d for d in insts if d.get("fb_t") is not None and not d.get("fb_pulado")]
    ax.errorbar([d["k"] for d in fb], [d["fb_t"] for d in fb], yerr=[d["fb_std"] for d in fb],
                fmt="s-", color=VERM, lw=2, ms=7, capsize=4, label="Forca Bruta")
    ax.set_yscale("log")
    ax.set_xlabel("Celulas vazias (K)")
    ax.set_ylabel("Tempo (ms, log)")
    ax.set_title("Tempo de execucao: Forca Bruta vs Backtracking")
    ax.grid(True, which="both", ls="--", alpha=0.4)
    ax.legend()
    fig.tight_layout()
    fig.savefig("grafico_tempo.png", dpi=150)
    plt.close(fig)
    print("grafico_tempo.png")


def grafico_estados_por_instancia(insts):
    fig, ax = plt.subplots(figsize=(10, 5.5))
    x = np.arange(len(insts))
    larg = 0.38

    bt = [d["bt_est"] for d in insts]
    fb = [d["fb_est"] for d in insts]
    pulado = [d.get("fb_pulado", False) for d in insts]

    ax.bar(x - larg / 2, bt, larg, color=AZUL, label="Backtracking (medido)")
    barras_fb = ax.bar(x + larg / 2, fb, larg, color=VERM, label="Forca Bruta (medido)")
    # marca as barras teoricas (FB pulada) com hachura
    for i, p in enumerate(pulado):
        if p:
            barras_fb[i].set_hatch("///")
            barras_fb[i].set_alpha(0.55)

    ax.set_yscale("log")
    ax.set_xticks(x)
    ax.set_xticklabels([rotulo(d["nome"], d["n"], d["k"]) for d in insts], fontsize=8)
    ax.set_ylabel("Estados explorados (log)")
    ax.set_title("Estados por instancia (barras hachuradas = limite teorico $2^K$, FB inviavel)")
    ax.grid(True, axis="y", which="both", ls="--", alpha=0.4)
    ax.legend()
    fig.tight_layout()
    fig.savefig("grafico_estados_por_instancia.png", dpi=150)
    plt.close(fig)
    print("grafico_estados_por_instancia.png")


def grafico_speedup(insts):
    fig, ax = plt.subplots(figsize=(10, 5))
    x = np.arange(len(insts))
    razao = [d["fb_est"] / d["bt_est"] for d in insts]
    cores = [VERM if d.get("fb_pulado") else "#ff9896" for d in insts]
    barras = ax.bar(x, razao, 0.6, color=cores)
    for i, d in enumerate(insts):
        if d.get("fb_pulado"):
            barras[i].set_hatch("///")
        ax.annotate(f"{razao[i]:.0f}x" if razao[i] < 1e4 else f"{razao[i]:.1e}x",
                    (x[i], razao[i]), textcoords="offset points", xytext=(0, 4),
                    ha="center", fontsize=8)
    ax.set_yscale("log")
    ax.set_xticks(x)
    ax.set_xticklabels([rotulo(d["nome"], d["n"], d["k"]) for d in insts], fontsize=8)
    ax.set_ylabel("Fator de reducao FB / BT (log)")
    ax.set_title("Quantas vezes a Forca Bruta explora mais estados que o Backtracking\n"
                 "(barras claras = medido; hachuradas = limite teorico, FB inviavel)")
    ax.grid(True, axis="y", which="both", ls="--", alpha=0.4)
    fig.tight_layout()
    fig.savefig("grafico_speedup.png", dpi=150)
    plt.close(fig)
    print("grafico_speedup.png")


def grafico_perfil_nivel(perfil):
    niveis = [int(r["nivel"]) for r in perfil]
    fb = [int(r["forca_bruta"]) for r in perfil]
    bt = [int(r["backtracking"]) for r in perfil]

    fig, ax = plt.subplots(figsize=(9, 5))
    ax.plot(niveis, fb, "s-", color=VERM, lw=2, ms=6, label="Forca Bruta")
    ax.plot(niveis, bt, "o-", color=AZUL, lw=2, ms=6, label="Backtracking")
    ax.set_yscale("log")
    ax.set_xlabel("Nivel da recursao (celula livre preenchida)")
    ax.set_ylabel("Estados explorados nesse nivel (log)")
    ax.set_title("Perfil da arvore de busca por profundidade (instancia 6x6, K=20)")
    ax.grid(True, which="both", ls="--", alpha=0.4)
    ax.legend()
    fig.tight_layout()
    fig.savefig("grafico_perfil_nivel.png", dpi=150)
    plt.close(fig)
    print("grafico_perfil_nivel.png")


def main():
    bench = sys.argv[1] if len(sys.argv) > 1 else "benchmark_tango.csv"
    perfil_path = sys.argv[2] if len(sys.argv) > 2 else "perfil_nivel.csv"

    insts = por_instancia(ler_csv(bench))
    grafico_estados(insts)
    grafico_tempo(insts)
    grafico_estados_por_instancia(insts)
    grafico_speedup(insts)
    grafico_perfil_nivel(ler_csv(perfil_path))
    print("Concluido.")


if __name__ == "__main__":
    main()
