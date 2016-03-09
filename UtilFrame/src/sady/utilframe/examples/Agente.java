package sady.utilframe.examples;


import  sady.utilframe.bdControl.DBObject;
import java.util.Calendar;

public class Agente extends DBObject {
    /** Field (ident_agente) Type(Integer) DataType(INT). */
    public static String identAgente = "ident_agente";
    /** Field (ident_gerente) Type(Integer) DataType(SMALLINT). */
    public static String identGerente = "ident_gerente";
    /** Field (endereco_agente) Type(String) DataType(VARCHAR). */
    public static String enderecoAgente = "endereco_agente";
    /** Field (tipo_agente) Type(Boolean) DataType(TINYINT). */
    public static String tipoAgente = "tipo_agente";
    /** Field (linha_comando) Type(String) DataType(VARCHAR). */
    public static String linhaComando = "linha_comando";
    /** Field (diretorio_log) Type(String) DataType(VARCHAR). */
    public static String diretorioLog = "diretorio_log";
    /** Field (configuracao) Type(String) DataType(CLOB). */
    public static String configuracao = "configuracao";
    /** Field (comandos_es) Type(String) DataType(CLOB). */
    public static String comandosEs = "comandos_es";
    /** Field (versao) Type(String) DataType(CHAR). */
    public static String versao = "versao";
    /** Field (data_alarme) Type(Calendar) DataType(TIMESTAMP). */
    public static String dataAlarme = "data_alarme";
    /** Field (data_expiracao) Type(Calendar) DataType(TIMESTAMP). */
    public static String dataExpiracao = "data_expiracao";

    @Override
    public String getConectionId() {
        return "id";
    }

    @Override
    public String getTableName() {
        return "agente";
    }

    public Integer getIdentAgente() {
        return (Integer) super.get(Agente.identAgente);
    }
    public void setIdentAgente(Integer identAgente) {
        super.set(Agente.identAgente, identAgente);
    }

    public Integer getIdentGerente() {
        return (Integer) super.get(Agente.identGerente);
    }
    public void setIdentGerente(Integer identGerente) {
        super.set(Agente.identGerente, identGerente);
    }

    public String getEnderecoAgente() {
        return (String) super.get(Agente.enderecoAgente);
    }
    public void setEnderecoAgente(String enderecoAgente) {
        super.set(Agente.enderecoAgente, enderecoAgente);
    }

    public boolean isTipoAgente() {
        return (Boolean) super.get(Agente.tipoAgente);
    }
    public void setTipoAgente(boolean tipoAgente) {
        super.set(Agente.tipoAgente, tipoAgente);
    }

    public String getLinhaComando() {
        return (String) super.get(Agente.linhaComando);
    }
    public void setLinhaComando(String linhaComando) {
        super.set(Agente.linhaComando, linhaComando);
    }

    public String getDiretorioLog() {
        return (String) super.get(Agente.diretorioLog);
    }
    public void setDiretorioLog(String diretorioLog) {
        super.set(Agente.diretorioLog, diretorioLog);
    }

    public String getConfiguracao() {
        return (String) super.get(Agente.configuracao);
    }
    public void setConfiguracao(String configuracao) {
        super.set(Agente.configuracao, configuracao);
    }

    public String getComandosEs() {
        return (String) super.get(Agente.comandosEs);
    }
    public void setComandosEs(String comandosEs) {
        super.set(Agente.comandosEs, comandosEs);
    }

    public String getVersao() {
        return (String) super.get(Agente.versao);
    }
    public void setVersao(String versao) {
        super.set(Agente.versao, versao);
    }

    public Calendar getDataAlarme() {
        return (Calendar) super.get(Agente.dataAlarme);
    }
    public void setDataAlarme(Calendar dataAlarme) {
        super.set(Agente.dataAlarme, dataAlarme);
    }

    public Calendar getDataExpiracao() {
        return (Calendar) super.get(Agente.dataExpiracao);
    }
    public void setDataExpiracao(Calendar dataExpiracao) {
        super.set(Agente.dataExpiracao, dataExpiracao);
    }

}
