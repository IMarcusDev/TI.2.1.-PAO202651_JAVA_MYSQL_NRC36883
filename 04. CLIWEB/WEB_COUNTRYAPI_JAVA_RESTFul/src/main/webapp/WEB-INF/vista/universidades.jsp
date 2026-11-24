<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Universidades (ECUD10): un solo formulario con «Importar» (POST) y «Ver guardadas». --%>
<c:set var="titulo" value="Universidades"/>
<c:set var="pantalla" value="universidades"/>
<%@ include file="partes/inicio.jspf" %>

<h1 class="titulo">Universidades</h1>
<p class="descripcion">Importa desde la API de universidades las de un país (escrito en inglés) y consulta las guardadas.</p>
<%@ include file="partes/avisos.jspf" %>

<form method="post" action="${ctx}/universidades" class="fila-formulario" novalidate>
    <%@ include file="partes/csrf.jspf" %>
    <div class="campo">
        <label for="pais">País (en inglés)</label>
        <input id="pais" name="pais" value="<c:out value='${pais}'/>">
    </div>
    <button class="btn" type="submit" name="accion" value="importar">Importar</button>
    <button class="btn btn--secundario" type="submit" name="accion" value="listar">Ver guardadas</button>
</form>

<c:if test="${universidades != null}">
    <p class="total">${universidades.size()} ${universidades.size() == 1 ? 'universidad' : 'universidades'}</p>
</c:if>
<c:if test="${not empty universidades}">
    <div class="tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr><th scope="col">Nombre</th><th scope="col">Dominio</th><th scope="col">Sitio web</th></tr>
            </thead>
            <tbody>
            <c:forEach var="u" items="${universidades}">
                <tr>
                    <td><c:out value="${u.nombre}"/></td>
                    <td><c:out value="${u.dominio}"/></td>
                    <td>
                        <%-- Solo direcciones http(s): un «javascript:» de la API nunca se vuelve enlace. --%>
                        <c:choose>
                            <c:when test="${fn:startsWith(u.sitioWeb, 'http://') || fn:startsWith(u.sitioWeb, 'https://')}">
                            <a href="<c:out value='${u.sitioWeb}'/>" target="_blank" rel="noopener noreferrer"><c:out value="${u.sitioWeb}"/></a>
                            </c:when>
                            <c:otherwise><c:out value="${u.sitioWeb}"/></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</c:if>

<%@ include file="partes/fin.jspf" %>
