package com.lucasmega.cursomc.services;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.lucasmega.cursomc.domain.PagamentoComBoleto;

@Service
public class BoletoService {
	
	public void preencherPagamentoComBoleto(PagamentoComBoleto pagamento, Date instanteDoPedido) {
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(instanteDoPedido);
		calendario.add(Calendar.DAY_OF_MONTH, 7);
		pagamento.setDataVencimento(calendario.getTime());
	}
}
