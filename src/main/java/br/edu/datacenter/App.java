
package br.edu.datacenter;

import br.edu.datacenter.gateway.Gateway;

public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando sistema Data Center...");

        Gateway gateway = new Gateway();
        gateway.start();
    }
}
