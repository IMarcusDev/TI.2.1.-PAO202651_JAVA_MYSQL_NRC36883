<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- Importar países (ECUD2): confirmación en la página y resumen de la importación. --%>
<c:set var="titulo" value="Importar países"/>
<c:set var="pantalla" value="importar"/>
<%@ include file="partes/inicio.jspf" %>

<h1 class="titulo">Importar países</h1>
<p class="descripcion">Trae todos los países de la API REST Countries. Los nuevos se registran y los existentes se actualizan, sin duplicar.</p>
<%@ include file="partes/avisos.jspf" %>

<c:choose>
    <c:when test="${confirmar}">
        <div class="tarjeta confirmacion">
            <p><strong>¿Importar los países desde la API REST Countries? Los países existentes se actualizarán</strong></p>
            <form method="post" action="${ctx}/importar" class="botones">
                <%@ include file="partes/csrf.jspf" %>
                <input type="hidden" name="confirmado" value="si">
                <button class="btn" type="submit">Importar</button>
                <a class="btn btn--secundario" href="${ctx}/importar">Cancelar</a>
            </form>
        </div>
    </c:when>
    <c:otherwise>
        <a class="btn" href="${ctx}/importar?confirmar">Importar países</a>
    </c:otherwise>
</c:choose>

<c:if test="${not empty resumen}">
    <h2 class="subtitulo">Resumen de la importación</h2>
    <div class="cifras">
        <div class="cifra"><span class="cifra__valor"><fmt:formatNumber value="${resumen.recibidos}"/></span>
            <span class="cifra__etiqueta">Recibidos de la API</span></div>
        <div class="cifra"><span class="cifra__valor"><fmt:formatNumber value="${resumen.registrados}"/></span>
            <span class="cifra__etiqueta">Registrados (nuevos)</span></div>
        <div class="cifra"><span class="cifra__valor"><fmt:formatNumber value="${resumen.actualizados}"/></span>
            <span class="cifra__etiqueta">Actualizados</span></div>
    </div>
</c:if>

<%@ include file="partes/fin.jspf" %>
