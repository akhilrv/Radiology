
<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Orders" otherwise="/login.htm"
	redirect="/module/radiology/radiologyOrder.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include
	file="/WEB-INF/view/module/radiology/resources/js/moreInfo.js"%>

<h2>
	<spring:message code="Order.title" />
</h2>

<spring:hasBindErrors name="order">
	<spring:message code="fix.error" />
	<br />
</spring:hasBindErrors>
<spring:hasBindErrors name="study">
	<spring:message code="fix.error" />
	<br />
</spring:hasBindErrors>

<c:if test="${order.voided}">
	<form method="post">
		<div class="retiredMessage">
			<div>
				<spring:message code="general.voidedBy" />
				${order.voidedBy.personName}
				<openmrs:formatDate date="${order.dateVoided}" type="medium" />
				- ${order.voidReason} <input type="submit" name="unvoidOrder"
					value='<spring:message code="Order.unvoidOrder"/>' />
			</div>
		</div>
	</form>
</c:if>
<form method="post" class="box">
	<input type="hidden" name="patient_id" value="${patientId}" />
	<spring:bind path="study.studyId">
		<input type="hidden" name="${status.expression}"
			value="${status.value}" />
	</spring:bind>
	<spring:bind path="study.mwlStatus">
		<input type="hidden" name="${status.expression}"
			value="${status.value}" />
		<c:if test="${status.errorMessage != ''}">
			<span class="error">${status.errorMessage}</span>
		</c:if>
	</spring:bind>
	<table>
		<tr>
			<td valign="top"><spring:message code="Order.patient" /></td>
			<td valign="top"><spring:bind path="order.patient">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.Patient"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
							<a style="cursor: pointer;" id="moreInfo"><spring:message
									code="radiology.moreInfo" /></a>
							<c:if test="${status.errorMessage != ''}">
								<span class="error">${status.errorMessage}</span>
							</c:if>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.editor.value.id}" />
							<input readonly="readonly" value="${order.patient.personName}" />
						</c:otherwise>
					</c:choose>
				</spring:bind></td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Order.concept" /></td>
			<td valign="top"><spring:bind path="order.concept">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.Concept"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
							<c:if test="${status.errorMessage != ''}">
								<span class="error">${status.errorMessage}</span>
							</c:if>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression }"
								value="${status.editor.value.id }" />
							<input readonly="readonly" value="${order.concept.name.name}" />
						</c:otherwise>
					</c:choose>
				</spring:bind>(Optional)</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="radiology.priority" /></td>
			<td valign="top"><spring:bind path="study.priority">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<select name="${status.expression}"
								id="requestedProcedurePrioritySelect">
								<c:forEach var="requestedProcedurePriority"
									items="${requestedProcedurePriorities}">
									<option value="${requestedProcedurePriority}"
										${status.value == requestedProcedurePriority ? 'selected="selected"' : ''}>${requestedProcedurePriority}</option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.value}" />
							<input readonly="readonly" value="${status.value}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr
			<c:if test="${!isUserScheduler && !isUserSuper}">style="display:none"</c:if>>
			<td valign="top"><spring:message
					code="radiology.scheduledStatus" /></td>
			<td valign="top"><spring:bind path="study.scheduledStatus">
					<c:choose>
						<c:when test="${isUserScheduler || isUserSuper}">
							<select name="${status.expression}"
								id="scheduledProcedureStepStatusSelect">
								<c:forEach var="scheduledProcedureStepStatus"
									items="${scheduledProcedureStepStatuses}">
									<option value="${scheduledProcedureStepStatus.key}"
										${status.value == scheduledProcedureStepStatus.key ? 'selected="selected"' : ''}>${scheduledProcedureStepStatus.value}</option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.value}" />
							<input readonly="readonly" value="${status.value}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr
			<c:if test="${!isUserSuper && !isUserPerformingPhysician}">style="display:none"</c:if>>
			<td valign="top"><spring:message
					code="radiology.performedStatus" /></td>
			<td valign="top"><spring:bind path="study.performedStatus">
					<c:choose>
						<c:when test="${isUserPerformingPhysician || isUserSuper}">
							<select name="${status.expression}" id="performedStatusSelect">
								<c:forEach var="performedStatus" items="${performedStatuses}">
									<option value="${performedStatus.key}"
										${status.value == performedStatus.key ? 'selected="selected"' : ''}><spring:message
											code="radiology.${performedStatus.key}"
											text="${performedStatus.value}" /></option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.value}" />
							<input readonly="readonly" value="${status.value}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="radiology.modality" /></td>
			<td valign="top"><spring:bind path="study.modality">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<select name="${status.expression}" id="modalitySelect">
								<c:forEach var="modality" items="${modalities}">
									<option value="${modality.key}"
										${status.value == modality.key ? 'selected="selected"' : ''}>${modality.value}</option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.value }" />
							<input readonly="readonly" value="${status.value}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.instructions" />
			</td>
			<td valign="top"><spring:bind path="order.instructions">
					<textarea name="${status.expression}"
						<c:if test="${!isUserReferringPhysician && !isUserSuper }">readonly="readonly"</c:if>>${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Order.encounter" /></td>
			<td valign="top"><spring:bind path="order.encounter">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.Encounter"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
							<c:if test="${status.errorMessage != ''}">
								<span class="error">${status.errorMessage}</span>
							</c:if>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression }"
								value="${status.editor.value.id }" />
							<input readonly="readonly" value="${order.encounter}" />
						</c:otherwise>
					</c:choose>
				</spring:bind>(Optional)</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Order.orderer" /></td>
			<td valign="top"><spring:bind path="order.orderer">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.User"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.editor.value.id}" />
							<input readonly="readonly" value="${order.orderer.personName}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind>(Default value is logged in User)</td>
		</tr>
		<c:if test="${isUserScheduler || isUserSuper}">
			<tr>
				<td valign="top"><spring:message code="general.dateStart" /></td>
				<td valign="top"><spring:bind path="order.startDate">
						<openmrs:fieldGen type="java.util.Date"
							formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}">
							<span class="error">${status.errorMessage}</span>
						</c:if>
					</spring:bind></td>
			</tr>
			<tr>
				<td valign="top"><spring:message code="general.dateAutoExpire" />
				</td>
				<td valign="top"><spring:bind path="order.autoExpireDate">
						<openmrs:fieldGen type="java.util.Date"
							formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}">
							<span class="error">${status.errorMessage}</span>
						</c:if>
					</spring:bind></td>
			</tr>
		</c:if>
		<tr <c:if test="${!isUserSuper}">style="display:none"</c:if>>
			<td valign="top"><spring:message code="radiology.scheduler" /></td>
			<td valign="top"><spring:bind path="study.scheduler">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.User"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.editor.value.id}" />
							<input readonly="readonly" value="${study.scheduler.personName}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr <c:if test="${!isUserSuper}">style="display:none"</c:if>>
			<td valign="top"><spring:message
					code="radiology.performingPhysician" /></td>
			<td valign="top"><spring:bind path="study.performingPhysician">
					<c:choose>
						<c:when test="${isUserReferringPhysician || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.User"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression }"
								value="${status.editor.value.id }" />
							<input readonly="readonly"
								value="${study.performingPhysician.personName}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<tr
			<c:if test="${isUserReferringPhysician}">style="display:none"</c:if>>
			<td valign="top"><spring:message
					code="radiology.readingPhysician" /></td>
			<td valign="top"><spring:bind path="study.readingPhysician">
					<c:choose>
						<c:when test="${isUserScheduler || isUserSuper}">
							<openmrs:fieldGen type="org.openmrs.User"
								formFieldName="${status.expression}"
								val="${status.editor.value}" />
						</c:when>
						<c:otherwise>
							<input type="hidden" name="${status.expression}"
								value="${status.editor.value.id}" />
							<input readonly="readonly"
								value="${study.readingPhysician.personName}" />
						</c:otherwise>
					</c:choose>
					<c:if test="${status.errorMessage != ''}">
						<span class="error">${status.errorMessage}</span>
					</c:if>
				</spring:bind></td>
		</tr>
		<c:if test="${order.discontinued}">
			<tr id="discontinuedBy">
				<td valign="top"><spring:message code="general.discontinuedBy" />
				</td>
				<td valign="top">${order.discontinuedBy.personName}</td>
			</tr>
			<tr id="dateDiscontinued">
				<td valign="top"><spring:message
						code="general.dateDiscontinued" /></td>
				<td valign="top"><openmrs:formatDate
						date="${order.discontinuedDate}" type="long" /></td>
			</tr>
			<tr id="discontinuedReason">
				<td valign="top"><spring:message
						code="general.discontinuedReason" /></td>
				<td valign="top">${order.discontinuedReason.name}</td>
			</tr>
		</c:if>
		<c:if test="${order.creator != null}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>${order.creator.personName}-<openmrs:formatDate
						date="${order.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
	<br /> <input type="submit" name="saveOrder"
		value="<spring:message code="Order.save"/>">
</form>


<c:if test="${order.discontinued}">
	<br />
	<form method="post" class="box">
		<input type="submit"
			value='<spring:message code="Order.undiscontinueOrder"/>'
			name="undiscontinueOrder" />
	</form>
</c:if>

<c:if test="${not order.discontinued and not empty order.orderId}">
	<br />
	<form method="post" class="box">
		<table>
			<tr id="dateDiscontinued">
				<td valign="top"><spring:message
						code="general.dateDiscontinued" /></td>
				<td valign="top"><spring:bind path="order.discontinuedDate">
						<openmrs:fieldGen type="java.util.Date"
							formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}">
							<span class="error">${status.errorMessage}</span>
						</c:if>
					</spring:bind></td>
			</tr>
			<tr id="discontinuedReason">
				<td valign="top"><spring:message
						code="general.discontinuedReason" /></td>
				<td valign="top"><spring:bind path="order.discontinuedReason">
						<openmrs:fieldGen type="org.openmrs.Concept"
							formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}">
							<span class="error">${status.errorMessage}</span>
						</c:if>
					</spring:bind></td>
			</tr>
		</table>
		<input type="submit" name="discontinueOrder"
			value='<spring:message code="Order.discontinueOrder"/>' />
	</form>
</c:if>

<c:if test="${not order.voided and not empty order.orderId}">
	<br />
	<form method="post" class="box">
		<spring:message code="general.voidReason" />
		<spring:bind path="order.voidReason">
			<input type="text" value="${status.value}" size="40"
				name="${status.expression }" />
			<spring:hasBindErrors name="order">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'voidReason'}">
						<span class="error"><spring:message
								code="${error.defaultMessage}" text="${error.defaultMessage}" />
						</span>
					</c:if>
				</c:forEach>
			</spring:hasBindErrors>
		</spring:bind>
		<input type="submit" name="voidOrder"
			value='<spring:message code="Order.voidOrder"/>' />
	</form>
</c:if>

<div id="moreInfoPopup"></div>
<%@ include file="/WEB-INF/template/footer.jsp"%>