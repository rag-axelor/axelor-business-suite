/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ImportPaymentVoucher {
	
	@Inject
	PaymentVoucherLoadService paymentVoucherLoadService;
	
	@Inject
	PaymentVoucherConfirmService paymentVoucherConfirmService;
	
	
	public Object importPaymentVoucher(Object bean, Map values) {
		assert bean instanceof PaymentVoucher;
		try{
			PaymentVoucher paymentVoucher = (PaymentVoucher)bean;
			Invoice invoiceToPay = getInvoice((String) values.get("orderImport"));
			paymentVoucher.setInvoiceToPay(invoiceToPay);
			paymentVoucherLoadService.loadMoveLines(paymentVoucher);
			if(paymentVoucher.getStatusSelect() == PaymentVoucherRepository.STATUS_CONFIRMED)
				paymentVoucherConfirmService.confirmPaymentVoucher(paymentVoucher);
			return paymentVoucher;
		}catch(Exception e){
	            e.printStackTrace();
	    }
		return bean;
	}

	public Invoice getInvoice(String orderType_orderImportId) {
		if (!Strings.isNullOrEmpty(orderType_orderImportId)) {
			String orderType = orderType_orderImportId.split("_")[0];
			String orderImportId = orderType_orderImportId.split("_")[1];

			String filter;
			if (orderType.equals("S")) {
				filter = "self.saleOrder.importId = ?";
			} else {
				filter = "self.purchaseOrder.importId = ?";
			}

			return Beans.get(InvoiceRepository.class).all().filter(filter, orderImportId).fetchOne();
		}
		return null;
	}
}
