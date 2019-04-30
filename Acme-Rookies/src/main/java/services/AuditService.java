
package services;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import repositories.AuditRepository;
import repositories.AuditorRepository;
import security.LoginService;
import security.UserAccount;
import domain.Audit;
import domain.Auditor;
import domain.Position;

@Service
@Transactional
public class AuditService {

	@Autowired
	private AuditRepository		auditRepository;
	@Autowired
	private ActorService		actorS;
	@Autowired
	private AuditorRepository	auditorRepository;
	@Autowired
	private Validator			validator;


	public Audit create() {
		final Audit audit = new Audit();
		final UserAccount user = this.actorS.getActorLogged().getUserAccount();
		audit.setMoment(new Date());
		audit.setScore(0);
		audit.setText("");
		audit.setDraftMode(1);
		audit.setPosition(new Position());
		audit.setAuditor(this.auditorRepository.auditorUserAccount(user.getId()));
		return audit;
	}

	public Collection<Audit> findAll() {
		return this.auditRepository.findAll();
	}

	public Audit findOne(final Integer auditId) {
		final Audit audit = this.auditRepository.findOne(auditId);
		final UserAccount userAccount = LoginService.getPrincipal();
		final Auditor auditor = this.auditorRepository.auditorUserAccount(userAccount.getId());
		Assert.isTrue(userAccount.getAuthorities().iterator().next().getAuthority().equals("AUDITOR"));
		Assert.isTrue(audit.getAuditor() == auditor);

		return audit;
	}

	public Audit save(final Audit audit) {
		final UserAccount userAccount = LoginService.getPrincipal();
		Assert.isTrue(userAccount.getAuthorities().iterator().next().getAuthority().equals("AUDITOR"));
		Assert.isTrue(audit.getAuditor().equals(this.auditorRepository.auditorUserAccount(userAccount.getId())));
		if (audit.getId() != 0) {
			final Audit old = this.findOne(audit.getId());
			Assert.isTrue(old.getDraftMode() == 1);
		}
		final Audit auditSave = this.auditRepository.save(audit);
		return auditSave;
	}

	public void delete(final Audit audit) {
		final UserAccount userAccount = LoginService.getPrincipal();
		Assert.isTrue(userAccount.getAuthorities().iterator().next().getAuthority().equals("AUDITOR"));
		Assert.isTrue(audit.getAuditor().equals(this.auditorRepository.auditorUserAccount(userAccount.getId())));
		Assert.isTrue(audit.getDraftMode() == 1);
		this.auditRepository.delete(audit);
	}

	public Collection<Audit> getAuditsByAuditor(final Integer auditorId) {
		return this.auditRepository.getAuditsByAuditor(auditorId);
	}

	public Audit reconstruct(final Audit audit, final BindingResult binding) {
		Audit res = new Audit();
		if (audit.getId() == 0) {
			res = audit;
			final UserAccount user = LoginService.getPrincipal();
			final Auditor auditor = this.auditorRepository.auditorUserAccount(user.getId());
			res.setMoment(new Date());
			res.setDraftMode(1);
			res.setPosition(new Position());
			res.setAuditor(auditor);
			this.validator.validate(res, binding);
		} else {
			res = this.auditRepository.findOne(audit.getId());
			final Audit a = new Audit();
			a.setId(res.getId());
			a.setVersion(res.getVersion());
			a.setMoment(res.getMoment());
			a.setText(audit.getText());
			a.setScore(audit.getScore());
			a.setDraftMode(audit.getDraftMode());
			a.setPosition(res.getPosition());
			a.setAuditor(res.getAuditor());
			this.validator.validate(a, binding);
			res = a;
		}
		return res;
	}

	//Dashboard
	public List<Object[]> getAvgMinMaxDesvScoreOfAudit() {
		return this.auditRepository.getAvgMinMaxDesvScoreOfAudit();
	}

}
