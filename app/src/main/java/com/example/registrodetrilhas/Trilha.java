package com.example.registrodetrilhas;

public class Trilha {
        private int id;
        private String nome;
        private String dataInicio;
        private String dataFim;
        private double velocidadeMedia;
        private double velocidadeMaxima;
        private double distancia;
        private int duracao;

        public Trilha(int id, String nome, String dataInicio, String dataFim,
                      double velocidadeMedia, double velocidadeMaxima,
                      double distancia, int duracao) {

            this.id = id;
            this.nome = nome;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.velocidadeMedia = velocidadeMedia;
            this.velocidadeMaxima = velocidadeMaxima;
            this.distancia = distancia;
            this.duracao = duracao;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getDataInicio() {
            return dataInicio;
        }

        public String getDataFim() {
            return dataFim;
        }

        public double getVelocidadeMedia() {
            return velocidadeMedia;
        }

        public double getVelocidadeMaxima() {
            return velocidadeMaxima;
        }

        public double getDistancia() {
            return distancia;
        }

        public int getDuracao() {
            return duracao;
        }
    }


