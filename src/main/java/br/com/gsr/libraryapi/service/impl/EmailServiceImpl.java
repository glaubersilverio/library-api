package br.com.gsr.libraryapi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.gsr.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

	@Value("${application.mail.default-rementent}")
	private String remetent;
	
	private final JavaMailSender javaMailSender;
	
	@Override
	public void sendMails(String mensagem, List<String> emailsList) {

		String[] mails = emailsList.toArray(new String[emailsList.size()]);
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(remetent);
		mailMessage.setSubject("Livro com empréstimo atrasado");
		mailMessage.setText(mensagem);
		mailMessage.setTo(mails);
		
		javaMailSender.send(mailMessage);
		
	}

}
