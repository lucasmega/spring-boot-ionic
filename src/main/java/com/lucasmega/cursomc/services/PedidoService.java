package com.lucasmega.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.lucasmega.cursomc.domain.Cliente;
import com.lucasmega.cursomc.domain.ItemPedido;
import com.lucasmega.cursomc.domain.PagamentoComBoleto;
import com.lucasmega.cursomc.domain.Pedido;
import com.lucasmega.cursomc.domain.enums.EstadoPagamento;
import com.lucasmega.cursomc.repositories.ItemPedidoRepository;
import com.lucasmega.cursomc.repositories.PagamentoRepository;
import com.lucasmega.cursomc.repositories.PedidoRepository;
import com.lucasmega.cursomc.security.UserSpringSecurity;
import com.lucasmega.cursomc.services.exceptions.AuthorizationException;
import com.lucasmega.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repository;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PedidoRepository pedidoRepository;
	
	public Pedido find(Integer id) {
		System.out.println("ID =>" +  id);
		Optional<Pedido> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! ID: " + id + " Tipo: " + Pedido.class.getName()));
	}
	
	
	public Pedido insert(Pedido obj) {
		obj.setId(null); 
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagamento = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagamento, obj.getInstante());
		}
		obj = repository.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		
		for (ItemPedido ip: obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		
		//emailService.sendOrderConfirmationEmail(obj);
		emailService.sendOrderConfirmationHtmlEmail(obj);
		
		return obj;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		
		UserSpringSecurity user = UserService.authenticated();
		
		if (user == null) {
			throw new AuthorizationException("Acesso negado!");
		}
		
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		Cliente cliente =  clienteService.find(user.getId());
		return pedidoRepository.findByCliente(cliente, pageRequest);
	}
	
}
