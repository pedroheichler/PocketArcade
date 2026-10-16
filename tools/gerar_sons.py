"""Gera os efeitos sonoros do Pocket Arcade (res/raw) por síntese, sem arquivos de terceiros.

Uso:  python tools/gerar_sons.py
Cada som é uma sequência de notas em onda quadrada (timbre de fliperama 8 bits)
com envelope de ataque/decaimento, gravada em WAV mono 16 bits a 22 050 Hz.
"""
import math
import os
import random
import struct
import wave

TAXA = 22050
VOLUME = 0.32
SAIDA = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "res", "raw")


def nota(freq, dur, duty=0.25, deslize=0.0):
    """Onda quadrada com deslize de frequência opcional e envelope suave."""
    total = int(TAXA * dur)
    amostras = []
    fase = 0.0
    ataque = max(1, int(total * 0.05))
    for i in range(total):
        f = freq + deslize * (i / total)
        fase = (fase + f / TAXA) % 1.0
        onda = 1.0 if fase < duty else -1.0
        if i < ataque:
            env = i / ataque
        else:
            env = (1.0 - (i - ataque) / (total - ataque)) ** 1.6
        amostras.append(onda * env)
    return amostras


def silencio(dur):
    return [0.0] * int(TAXA * dur)


def suavizar(amostras, fator=0.35):
    """Filtro passa-baixa simples para tirar a aspereza da onda quadrada."""
    saida, anterior = [], 0.0
    for a in amostras:
        anterior = anterior + fator * (a - anterior)
        saida.append(anterior)
    return saida


def gravar(nome, amostras):
    caminho = os.path.normpath(os.path.join(SAIDA, nome))
    with wave.open(caminho, "wb") as arquivo:
        arquivo.setnchannels(1)
        arquivo.setsampwidth(2)
        arquivo.setframerate(TAXA)
        dados = b"".join(struct.pack("<h", int(max(-1.0, min(1.0, a)) * VOLUME * 32767)) for a in suavizar(amostras))
        arquivo.writeframes(dados)
    print(f"{caminho}  ({len(amostras) / TAXA:.2f} s)")


def hz(semitons_desde_la4):
    return 440.0 * math.pow(2, semitons_desde_la4 / 12)


def vitoria():
    # Arpejo ascendente de Dó maior terminando em nota longa.
    s = []
    for semi, dur in [(3, 0.08), (7, 0.08), (10, 0.08), (15, 0.30)]:
        s += nota(hz(semi), dur, duty=0.25)
    return s


def derrota():
    # Três notas descendo com deslize para o grave.
    s = []
    s += nota(hz(-2), 0.14, duty=0.5)
    s += nota(hz(-5), 0.14, duty=0.5)
    s += nota(hz(-9), 0.36, duty=0.5, deslize=-60)
    return s


def empate():
    # Dois bipes iguais e neutros.
    return nota(hz(0), 0.09, duty=0.125) + silencio(0.06) + nota(hz(0), 0.12, duty=0.125)


def sorteio():
    # Roleta de bipes rápidos acelerando e um "ding" final.
    gerador = random.Random(5)
    s = []
    duracoes = [0.05, 0.05, 0.045, 0.045, 0.04, 0.04, 0.04, 0.05, 0.06]
    for d in duracoes:
        s += nota(hz(gerador.choice([2, 5, 7, 9, 12, 14])), d, duty=0.25)
    s += silencio(0.03)
    s += nota(hz(19), 0.32, duty=0.125)
    return s


if __name__ == "__main__":
    os.makedirs(SAIDA, exist_ok=True)
    gravar("som_vitoria.wav", vitoria())
    gravar("som_derrota.wav", derrota())
    gravar("som_empate.wav", empate())
    gravar("som_sorteio.wav", sorteio())
