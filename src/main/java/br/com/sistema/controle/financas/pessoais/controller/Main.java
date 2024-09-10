package br.com.sistema.controle.financas.pessoais.controller;

import br.com.sistema.controle.financas.pessoais.model.conta.ContaEntity;
import br.com.sistema.controle.financas.pessoais.model.usuario.UsuarioEntity;
import br.com.sistema.controle.financas.pessoais.service.conta.ContaService;
import br.com.sistema.controle.financas.pessoais.service.conta.TransacaoService;
import br.com.sistema.controle.financas.pessoais.service.usuario.UsuarioService;
import br.com.sistema.controle.financas.pessoais.utils.Constantes;
import br.com.sistema.controle.financas.pessoais.utils.FuncoesUtil;
import br.com.sistema.controle.financas.pessoais.utils.validacoes.ValidarEmail;
import br.com.sistema.controle.financas.pessoais.utils.validacoes.ValidarNome;
import br.com.sistema.controle.financas.pessoais.utils.validacoes.ValidarNumeroCelular;
import br.com.sistema.controle.financas.pessoais.utils.validacoes.ValidarSenha;
import static br.com.sistema.controle.financas.pessoais.utils.validacoes.ValidarNumeroCelular.formatarNumeroCelular;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;


public class Main {

    static UsuarioService usuarioService = new UsuarioService();
    static ContaService contaService = new ContaService();
    static TransacaoService transacaoService = new TransacaoService();


    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        Constantes.aberturaSistema();

        while (true) {
            System.out.println(Constantes.bemVindo);
            System.out.println("(1) Cadastrar Novo Usuario");
            System.out.println("(2) Fazer login");
            System.out.println("(3) Sair");

            String escolhaOpcao = input.nextLine();
            if (!FuncoesUtil.ehNumero(escolhaOpcao)) {
                System.err.println(Constantes.OpcaoInvalida);
                continue;
            }
            int escolha = Integer.parseInt(escolhaOpcao);

            switch (escolha) {
                case 1:
                    cadastrarNovoUsuario(input);
                    break;
                case 2:
                    realizarLogin(input);
                    break;
                case 3:
                    return;
                default:
                    System.err.println(Constantes.OpcaoInvalida);
            }
        }
    }

    public static void realizarLogin(Scanner input){

        System.out.println("----LOGIN---\n");
        System.out.println("Digite o E-MAIL: ");
        String email = input.nextLine();

        System.out.println("Digite a Senha: ");
        String senha = input.nextLine();

        UsuarioEntity loginValido = usuarioService.autenticarUsuario(email, senha);

        if (loginValido == null){
            System.err.println(Constantes.erroLoginConta);
            return;
        }

        Integer idUsuario = usuarioService.obterIdUsuarioPorEmail(email);

        if (idUsuario == null){
            System.err.println(Constantes.erroLoginConta);
            return;
        }
        System.out.println(Constantes.loginConta);
        usuarioLogado(input, idUsuario);
    }

    public static void usuarioLogado(Scanner input, Integer idUsuario) {
        while (true) {

            Double saldoAtual = contaService.obterSaldo(idUsuario);
            System.out.println("SEU SALDO R$: " + saldoAtual);

            List<ContaEntity> contas = contaService.obterContasPorIdUsuario(idUsuario);

            if (contas.isEmpty()){
                System.err.println(Constantes.contaNaoEncontrada);
                return;
            }

            for (int i = 0; i < contas.size(); i++){
                ContaEntity conta = contas.get(i);
                System.out.println("Conta: " + conta.getNomeConta() +
                        " | Saldo R$ " + conta.getSaldoConta());
            }

            System.out.println("(1) Adicionar conta");
            System.out.println("Escolha a opção desejada: ");
            System.out.println("(2) Realizar Transação");
            System.out.println("Escolha a opção desejada: ");
            System.out.println("(3) Deslogar");
            System.out.println("Escolha a opção desejada: ");

            String logadoUsuario = input.nextLine();
            if (!FuncoesUtil.ehNumero(logadoUsuario)) {
                System.err.println(Constantes.OpcaoInvalida);
                continue;
            }
            int opcaoEscolha = Integer.parseInt(logadoUsuario);

            switch (opcaoEscolha) {
                case 1:
                    cadastrarConta(input, idUsuario, idUsuario);
                    break;
                case 2:
                    registrarTransacao(input, idUsuario);
                    break;
                case 3:
                    return;
                default:
                    System.out.println(Constantes.OpcaoInvalida);
            }
        }
    }

    public static void cadastrarNovoUsuario(Scanner input) {
        UsuarioEntity novoUsuario = new UsuarioEntity();

        do {
            String nomeUsuario = validarPrenchimentoEntrada(input,
                    "Digite o Nome Completo",
                    "Nome não preenchido");
            if (!ValidarNome.validarNome(nomeUsuario)) {
                System.err.println(Constantes.cadastroNome);
                continue;
            }
            novoUsuario.setNomeUsuario(nomeUsuario);
            break;
        } while (true);

        do {
            String emailUsuario = validarPrenchimentoEntrada(input,
                    "Digite o E-MAIL",
                    "E-MAIL não preenchido");
            if (!ValidarEmail.validaEmail(emailUsuario)) {
                System.err.println(Constantes.cadastroEmail);
                continue;
            }

            Boolean emailExiste = usuarioService.emailExiste(emailUsuario);
            if (emailExiste == true) {
                System.err.println(Constantes.EmailJaCadastrado);
                continue;
            }

            novoUsuario.setEmailUsuario(emailUsuario);
            break;
        } while (true);

        preencherSenha(input, novoUsuario);

        do {
            String numeroCelular = validarPrenchimentoEntrada(input,
                    "Digite o Número Celular",
                    "Número não preenchido");
            if (!ValidarNumeroCelular.validarNumeroCelular(numeroCelular)) {
                System.err.println(Constantes.cadastroCelular);
                continue;
            }
            novoUsuario.setNumeroCelular(formatarNumeroCelular(numeroCelular));
            break;
        } while (true);

        if (usuarioService.criarUsuario(novoUsuario) == null) {
            System.err.println(Constantes.ErroCadastroUsuario);
        }

        System.out.println(Constantes.CadastroRealizadoUsuario);
        System.out.println(Constantes.MensagemLoginUsuario);

    }

    private static void preencherSenha(Scanner input, UsuarioEntity novoUsuario) {

        String senha1;
        do {
            senha1 = validarPrenchimentoEntrada(input,
                    "Digite uma senha",
                    "Senha não preenchida");
            if (!ValidarSenha.isValidPassword(senha1)) {
                System.err.println(Constantes.cadastroSenha);
                continue;
            }
            novoUsuario.setSenhaUsuario(senha1);
            break;
        } while (true);

        do {
            String senha2 = validarPrenchimentoEntrada(input,
                    "Confirme novamente a senha",
                    "Senha não preenchida");
            if (!senha1.equals(senha2)) {
                System.err.println(Constantes.confirmacaoSenha);
                continue;
            }
            novoUsuario.setSenhaUsuario(senha1);
            break;
        } while (true);
    }

    private static String validarPrenchimentoEntrada(Scanner input,
                                                     String mensagemDeEntrada,
                                                     String mesagemDeErro) {
        while (true) {
            System.out.println(mensagemDeEntrada);
            String valor = input.nextLine();

            if (valor.isBlank()) {
                System.out.println(mesagemDeErro);
                continue;
            }
            return valor;
        }
    }

    public static void cadastrarConta(Scanner input, Integer idUsuario, Integer idSaldo) {
        ContaEntity novaConta = new ContaEntity();

        do {
            String nomeConta = validarPrenchimentoEntrada(input,
                    "Digite o Nome da Conta",
                    "Nome não preenchido");
            if (!ValidarNome.validarNome(nomeConta)){
                System.err.println(Constantes.cadastroNomeConta);
                continue;
            }
            novaConta.setNomeConta(nomeConta);
            break;
        } while (true);

        do {
            String saldo = validarPrenchimentoEntrada(input,
                    "Digite seu saldo atual",
                    "Saldo não preenchido");
            if (!FuncoesUtil.ehNumero(saldo)){
                System.err.println(Constantes.cadastroSaldo);
                continue;
            }
            novaConta.setSaldoConta(Double.parseDouble(saldo));
            break;
        }while (true);

        do {
            String tipoConta = validarPrenchimentoEntrada(input,
                    "Digite o Tipo da Conta",
                    "Tipo Conta não preenchido");
            if (!ValidarNome.validarNome(tipoConta)){
                System.err.println(Constantes.cadastroTipoConta);
                continue;
            }
            novaConta.setTipoConta(tipoConta);

            break;
        } while (true);

        novaConta.setIdUsuario(idUsuario);
        novaConta.setIdSaldo(idSaldo);
        novaConta.setDataDeposito(Timestamp.valueOf(LocalDateTime.now()));

        if (contaService.criarConta(novaConta) == null){
            System.err.println(Constantes.ErroCadastroConta);
        }
        System.out.println(Constantes.cadastroConta);
    }

    public static void registrarTransacao(Scanner input, Integer idUsuario) {

        List<ContaEntity> contas = contaService.obterContasPorIdUsuario(idUsuario);

        if (contas.isEmpty()){
            System.err.println(Constantes.contaNaoEncontrada);
            return;
        }

        System.out.println("Selecione a conta para realizar a transação: ");
        for (int i = 0; i < contas.size(); i++){
            ContaEntity conta = contas.get(i);
            System.out.println((i + 1) + ". Conta: " + conta.getNomeConta() +
                    " | Tipo conta: " + conta.getTipoConta() +
                    " | Saldo R$ " + conta.getSaldoConta());
        }

        System.out.println("\nDigite o número da conta:");
        int escolha = Integer.parseInt(input.nextLine());


        if (escolha < 1 || escolha > contas.size()){
            System.err.println("Opção invalida.");
            return;
        }

        ContaEntity contaSelecionada = contas.get(escolha - 1);

        System.out.println("Digite a descrição da transação:");
        String descricao = input.nextLine();

        System.out.println("Digite o valor da transação:");
        Double valor = Double.parseDouble(input.nextLine());

        System.out.println("Digite o tipo de transação (1 para receitas, 2 para despesas):");
        int tipo = Integer.parseInt(input.nextLine());

        if (tipo != 1 && tipo != 2){
            System.err.println(Constantes.tipoTransacao);
            return;
        }

        transacaoService.registrarTransacao(contaSelecionada.getIdConta(), contaSelecionada.getIdSaldo(), descricao, valor, tipo);

        System.out.println(Constantes.cadastroTransacao);
    }
}