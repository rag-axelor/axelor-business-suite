/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice.workflow.cancel;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.workflow.WorkflowInvoice;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class CancelState extends WorkflowInvoice {

	private WorkflowCancelService workflowService;

	@Inject
	CancelState(WorkflowCancelService workflowService) {
		this.workflowService = workflowService;
	}

	@Override
	public void init(Invoice invoice){
		this.invoice = invoice;
	}

	@Override
	public void process() throws AxelorException {

		workflowService.beforeCancel(invoice);

		if(invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED && invoice.getCompany().getAccountConfig().getAllowCancelVentilatedInvoice())  {
			cancelMove();
		}

		setStatus();
		workflowService.afterCancel(invoice);
	}

	protected void setStatus(){

		invoice.setStatusSelect(InvoiceRepository.STATUS_CANCELED);

	}

	protected void cancelMove() throws AxelorException{

		if (invoice.getOldMove() != null) {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.INVOICE_CANCEL_1));
		}
		
		Move move = invoice.getMove();
		
		invoice.setMove(null);
		
		Beans.get(MoveCancelService.class).cancel(move);
	}

}