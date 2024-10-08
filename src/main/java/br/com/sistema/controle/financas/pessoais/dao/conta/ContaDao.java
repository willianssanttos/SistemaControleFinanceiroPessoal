package br.com.sistema.controle.financas.pessoais.dao.conta;

import br.com.sistema.controle.financas.pessoais.model.conta.ContaEntity;

import java.util.List;

public interface ContaDao {

    ContaEntity criarConta(ContaEntity conta);

    List<ContaEntity> obterContasPorUsuario(Integer idUsuario);

    void excluirConta(Integer idConta);
    void editarConta(ContaEntity conta);
}
