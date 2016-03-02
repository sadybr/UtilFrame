package sady.utilframe.tools;


/**
 * Classe para manipulação de String em arquivos.
 * 
 * @author Sady Rodrigues
 * 
 * @version 1.4, 12/04/2007
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

public class FileTools {

    private static final String defaultConfigDir = "Files";
    private static final String defaultConfigFile = FileTools.defaultConfigDir + File.separatorChar + "conf.properties";
    
    
    private static Properties prop = new Properties();

    private BufferedReader reader;
    private BufferedWriter writer;
    private int bufferSize;
    private Vector<String> buffer;

    private String nextLine = null;
    private boolean fileClosed = false;

    /**
     * Construtor.
     * @param fileIn arquivo de leitura
     * @throws FileNotFoundException caso não encontre o arquivo de leitura
     */
    public FileTools(String fileIn) throws FileNotFoundException {
        this(fileIn, null, 5, false);
    }
    /**
     * Construtor.
     * @param fileIn arquivo de leitura
     * @param fileOut arquivo de escrita
     * @throws FileNotFoundException caso não encontre o arquivo de leitura
     */
    public FileTools(String fileIn, String fileOut) throws FileNotFoundException {
        this(fileIn, fileOut, 5, false);
    }
    /**
     * Construtor.
     * @param fileIn arquivo de leitura
     * @throws FileNotFoundException caso não encontre o arquivo de leitura
     */
    public FileTools(InputStream fileIn) throws FileNotFoundException {
        this(fileIn, null, 5);
    }
    /**
     * Construtor.
     * @param fileIn arquivo de leitura
     * @param fileOut arquivo de escrita
     * @throws FileNotFoundException caso não encontre o arquivo de leitura
     */
    public FileTools(InputStream fileIn, OutputStream fileOut) throws FileNotFoundException {
        this(fileIn, fileOut, 5);
    }

    /**
     * Construtor.
     * @param fileIn arquivo para leitura
     * @param fileOut arqurivo para gravação
     * @param type status de operação da classe.
     *        Ela indicará se foi intanciada para gravação em arquivo, leitura em arquivo ou ambos.
     * @param bufferSize tamanho do buffer de gravação
     * @param append se o arquivo de gravação vai manter os dados atuais
     * @throws FileNotFoundException caso não encontre algum dos arquivos especificados
     */
    public FileTools(InputStream fileIn, OutputStream fileOut, int bufferSize) throws FileNotFoundException {
        try {
            this.init(fileIn, fileOut, bufferSize, null, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    /**
     * Construtor.
     * @param fileIn arquivo para leitura
     * @param fileOut arqurivo para gravação
     * @param type status de operação da classe.
     *        Ela indicará se foi intanciada para gravação em arquivo, leitura em arquivo ou ambos.
     * @param bufferSize tamanho do buffer de gravação
     * @param append se o arquivo de gravação vai manter os dados atuais
     * @throws FileNotFoundException caso não encontre algum dos arquivos especificados
     */
    public FileTools(String fileIn, String fileOut, int bufferSize, boolean append) throws FileNotFoundException {
        try {
            FileInputStream in = null;
            FileOutputStream out = null;
            
            if (fileIn != null) {
                in = new FileInputStream(new File(fileIn));
            }
            if (fileOut != null) {
                out = new FileOutputStream(new File(fileOut), append);
            }
            
            this.init(in, out, bufferSize, null, null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    /**
     * Construtor.
     * @param fileIn arquivo para leitura
     * @param fileOut arqurivo para gravação
     * @param type status de operação da classe.
     *        Ela indicará se foi intanciada para gravação em arquivo, leitura em arquivo ou ambos.
     * @param bufferSize tamanho do buffer de gravação
     * @param append se o arquivo de gravação vai manter os dados atuais
     * @param charSetWriter nome do charSet para o arquivo de gravação
     * @param charSetReader nome do charSet para o arquivo de leitura
     * @throws FileNotFoundException caso não encontre algum dos arquivos especificados
     * @throws UnsupportedEncodingException charSet não suportado
     */
    public FileTools(String fileIn,
                     String fileOut, 
                     int bufferSize, 
                     boolean append,
                     String charSetReader,
                     String charSetWriter
                     ) throws FileNotFoundException, UnsupportedEncodingException {
        this.init(fileIn != null ? new FileInputStream(new File(fileIn)) : null, 
        		  fileOut != null ? new FileOutputStream(new File(fileOut), append) : null, 
        		  bufferSize, 
        		  charSetReader, 
        		  charSetWriter);
    }

    private void init(InputStream fileIn,
            OutputStream fileOut, 
            int bufferSize, 
            String charSetWriter,
            String charSetReader) throws FileNotFoundException, UnsupportedEncodingException {
      
        if (fileIn != null) {
            if (charSetReader != null) {
                this.reader = new BufferedReader(new InputStreamReader(fileIn, charSetReader));
            } else {
                this.reader = new BufferedReader(new InputStreamReader(fileIn));
            }
        } 
        if (fileOut != null) {
            if (charSetWriter != null) {
                this.writer = new BufferedWriter(new OutputStreamWriter(fileOut, charSetWriter));
            } else {
                this.writer = new BufferedWriter(new OutputStreamWriter(fileOut));
            }
        }
        this.bufferSize = bufferSize;
        this.buffer = new Vector<String>();
    }

    /**
     * Fecha o arquivo e grava tudo que estiver no buffer.
     * @throws IOException caso ocorra algum erro
     */
    public void closeWriter() throws IOException {
        if (this.writer != null) {
            this.flush();
            this.writer.close();
            this.writer = null;
        } else {
            System.out.println("INFO: Writer nulo");
        }

    }
    /**
     * Grava tudo que está no buffer.
     * @throws IOException caso ocorra algum erro
     */
    public void flush() throws IOException {
        if (this.writer != null) {
            for (String line : this.buffer) {
                this.writer.write(line);
                this.writer.newLine();
                this.writer.flush();
            }
            this.buffer.clear();
        } else {
            System.out.println("INFO: Writer nulo");
        }

        
    }

    /**
     * Salva o arquivo por linhas usando o buffer.
     * @param line linha
     * @throws IOException caso ocorra algum erro
     */
    public void writeLine(String line) throws IOException {
        if (this.writer == null) {
            throw new IllegalStateException("Escritor nulo");
        }

        this.buffer.add(line);

        if (this.buffer.size() % this.bufferSize == 0) {
            for (String bufferLine : this.buffer) {
                this.writer.write(bufferLine);
                this.writer.newLine();
                this.writer.flush();
            }
            this.buffer.clear();
        }
    }

    /**
     * Lê a próxima linha do arquivo com preview de 1 linha,
     * se o arquivo tiver só uma linha e o método for chamada uma vez somente,
     * o arquivo será fechado logo após a leitura da primeira linha.
     * quando o arquivo termina, retorna NULL e fecha a conexão com o aquivo.
     * @return próxima linha do arquivo
     * @throws IOException caso ocorra erro ao ler o arquivo
     */
    public String nextLine() throws IOException {
        if (this.reader == null) {
            throw new IllegalStateException("Leitor nulo");
        }

        if (this.nextLine == null && !this.fileClosed) {
        	// Lendo a primeira linha do arquivo
        	this.nextLine = this.reader.readLine(); 
        } else if(this.nextLine == null && this.fileClosed) {
        	// Ja terminou de ler o arquivo
        	return null;
        }

        // linha atual
        String line = this.nextLine;
        // gerando preview da proxima linha
        this.nextLine = this.reader.readLine();

        // verificando se o preview foi nulo para fechar o arquivo
        if (this.nextLine == null) {
            try {
                this.reader.close();
                this.fileClosed = true;
            } catch (IOException e1) {
            	System.err.println("Problemas para fechar o arquivo. " + e1.getMessage());
            }
        }
        
        return line;
    }

    /**
     * Salva a propriedade no arquivo de propriedade default
     * 
     * @param key Nome da propriedade
     * @param value Valor da propriedade
     */
    public static void saveProperties(String key, String value) {
        File file = new File(FileTools.defaultConfigDir);
        if (!file.isDirectory()) {
            file.mkdir();
            file = new File(FileTools.defaultConfigFile);
            try {
                file.createNewFile();
            } catch (IOException e) {
            	System.err.println("Erro ao criar o arquivo " + e.getMessage());
            }
        }
        FileTools.saveProperties(key, value, FileTools.defaultConfigFile);
    }

    /**
     * Recupera a propriedade do arquivo de propriedade default e retorna uma string vazia no caso de não encontrar
     * 
     * @param key Nome da propriedade
     */
    public static String getProperties(String key) {
        return FileTools.getProperties(key, FileTools.defaultConfigFile);
    }

    public static void saveProperties(String key, String value, String arquivo) {
    	saveProperties(key, value, arquivo, false);
    }
    /**
     * Salva a propriedade no arquivo de propriedade
     * 
     * @param key Nome da propriedade
     * @param value Valor da propriedade
     * @param arquivo Caminho do arquivo de propriedade
     */
    public static void saveProperties(String key, String value, String arquivo, boolean isRetry) {
        File arqProp = new File(arquivo);
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(arqProp));
        } catch (FileNotFoundException e) {
        	System.err.println("Arquivo nao encontrado");
        } catch (IOException e) {
        	System.err.println("Erro ao abrir o arquivo: " + e.getMessage());
        }
        prop.setProperty(key, value);
        try {
            prop.store(new FileOutputStream(arqProp), "Arquivo de Configuração");
        } catch (FileNotFoundException e) {
        	System.err.println("Arquivo não encontrado");
        	try {
        		if (!isRetry) {
        			File file = new File(arquivo);
        			if (file.createNewFile()) {
        				saveProperties(key, value, arquivo, true);
        			}
        		}
			} catch (IOException e1) {
				System.err.println("Erro ao atualizar o arquivo: " + e1.getMessage());
			}

        } catch (IOException e) {
        	System.err.println("Erro ao atualizar o arquivo: " + e.getMessage());
        }
    }

    /**
     * Recupera a propriedade no arquivo de propriedade e retorna uma string vazia no caso de não encontrar
     * 
     * @param key Nome da propriedade
     * @param arquivo Caminho do arquivo de propriedade
     * @return Valor da propriedade
     */
    public static String getProperties(String key, String arquivo) {
        try {
			return getProperties(key, new FileInputStream(new File(arquivo)));
		} catch (FileNotFoundException e) {
			System.err.println("Arquivo não encontrado");
		}
		return "";
    }

    /**
     * Recupera a propriedade no arquivo de propriedade e retorna uma string vazia no caso de não encontrar
     * 
     * @param key Nome da propriedade
     * @param arquivo Caminho do arquivo de propriedade
     * @return Valor da propriedade
     */
    public static String getProperties(String key, InputStream arquivo) {
    	try {
            prop.load(arquivo);
            return prop.getProperty(key, "");
        } catch (FileNotFoundException e) {
        	System.err.println("Arquivo não encontrado");
        } catch (IOException e) {
        	System.err.println("Erro ao atualizar o arquivo: " + e.getMessage());
        }
        return "";
    }

    /**
     * @param arquivo Nome do arquivo de leitura
     * @return Retorna um ArrayList com cada linha em uma posição
     * @throws IOException
     */
    @Deprecated
    public static ArrayList<String> lerArquivo(String arquivo) throws IOException {

        File file = new File(arquivo);
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader); 
        
        String line = reader.readLine();
        ArrayList<String> array = new ArrayList<String>();
        
        while (line != null) {
            array.add(line);
            line = reader.readLine();
        }
        
        try {
            reader.close();
        } catch (IOException e1) {
        	System.err.println("Problemas para fechar o Arquivo: " + arquivo + ": " + e1.getMessage());
        }
        return array;
    }

    /**
     * @param arquivo Nome do Arquivo de destino
     * @param dados ArrayList com os dados para gravar
     * @throws IOException
     */
    @Deprecated
    public static void salvar(String arquivo, ArrayList<String> dados, boolean append) throws IOException {

        int count = 0;

        File file;
        FileOutputStream FOSP;
        BufferedWriter BWR;

        // Prenchimento do novo arquivo
        file = new File(arquivo);
        file.createNewFile();
        FOSP = new FileOutputStream(file, append);
        BWR = new BufferedWriter(new OutputStreamWriter(FOSP, "utf-8"));

        while (dados.size() > count) {
            BWR.write(dados.get(count));
            count++;
            if (dados.size() > count)
                BWR.newLine();
        }
        BWR.close();
        FOSP.close();
    }

    /**
     * Salva uma string em um arquivo.
     * @param arquivo nome do arquivo
     * @param dados string a ser salva
     * @param append se é para adicionar ao final do arquivo
     * @throws IOException caso ocorra algum erro
     */
    @Deprecated
    public static void salvar(String arquivo, String dados, boolean append) throws IOException {
        ArrayList<String> al = new ArrayList<String>();
        al.add(dados);
        salvar(arquivo, al, append);
    }

    /**
     * Lê o arquivo como uma string.
     * @param arquivo nome do arquivo de origuem
     * @return string com tudo do arquivo
     * @throws IOException caso ocorra algum erro
     */
    @Deprecated
    public static String lerArquivoString(String arquivo) throws IOException {
        
        File file = new File(arquivo);
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader); 
        
        String line = reader.readLine();
        StringBuffer buffer = new StringBuffer();
        
        while (line != null) {
            buffer.append(line);
            line = reader.readLine();
        }
        
        try {
            reader.close();
        } catch (IOException e1) {
        	System.err.println("Problemas para fechar o Arquivo: " + arquivo + ": " + e1.getMessage());
        }
        return buffer.toString();
    }

}