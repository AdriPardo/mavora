import { Link } from "react-router-dom";

export function PrivacyPage() {
  return (
    <main className="mx-auto max-w-2xl px-4 py-12 sm:px-6">
      <p className="text-sm font-semibold tracking-tight">Mavora</p>
      <h1 className="mt-6 text-2xl font-semibold tracking-tight">Política de privacidad</h1>
      <p className="mt-2 text-sm text-zinc-500 dark:text-zinc-400">
        Última actualización: 9 de septiembre de 2026. Esta página es pública: no hace falta iniciar sesión.
      </p>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Quiénes somos</h2>
        <p>
          Mavora es un software de marketing autónomo. Esta política describe qué datos trata el producto cuando una
          organización usa la aplicación. No publicamos CIF, domicilio fiscal ni un dominio propio porque el producto
          aún no lista esos datos. Cuando Mavora esté en un sitio HTTPS público, esta misma ruta (
          <code className="rounded bg-zinc-100 px-1 text-xs dark:bg-zinc-900">/privacidad</code>) es la URL que hay que
          pegar en la app de Meta.
        </p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Qué datos tratamos</h2>
        <ul className="list-disc space-y-2 pl-5">
          <li>Cuenta: email y contraseña. La contraseña no se guarda en claro.</li>
          <li>
            Organización: nombre, ficha de empresa, objetivos, contenido, campañas y la analítica que tú registras (por
            ejemplo alcance y seguidores).
          </li>
          <li>
            Sesión: cookie <code className="rounded bg-zinc-100 px-1 text-xs dark:bg-zinc-900">mavora_session</code>{" "}
            (HttpOnly, SameSite=Lax) para mantenerte autenticado.
          </li>
          <li>
            Instagram, solo si conectas una cuenta Professional con la API oficial de Meta (OAuth o token de página):
            identificadores de cuenta/página y tokens de acceso. Los tokens y el App Secret se cifran; no vuelven a la
            interfaz.
          </li>
          <li>Medios: imágenes y vídeos que subes o que genera Mavora para publicar por Graph API.</li>
        </ul>
        <p>
          Mavora no scrapea Instagram, no inicia sesión en instagram.com y no automatiza la interfaz web. La publicación
          y la lectura de perfil van por Meta Graph API, o en modo de demostración (fake) sin cuenta real.
        </p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Cookies</h2>
        <p>Usamos la cookie de sesión necesaria para el login. No usamos cookies de publicidad de terceros en esta aplicación.</p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Para qué los usamos</h2>
        <p>
          Prestar el servicio: generar copy y visuales, guardar el calendario y, si activas la autonomía y hay conexión
          oficial, publicar en Instagram a través de Graph API.
        </p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Con quién se comparte</h2>
        <ul className="list-disc space-y-2 pl-5">
          <li>Meta / Instagram, solo si conectas la cuenta, para OAuth, perfil y publicación.</li>
          <li>Proveedores de generación de texto e imagen, cuando están configurados por la organización.</li>
        </ul>
        <p>No vendemos estos datos.</p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Conservación</h2>
        <p>Mientras tu organización exista en Mavora, salvo que pidas el borrado a quien la administra.</p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Tus derechos y contacto</h2>
        <p>
          Puedes pedir acceso, rectificación, supresión, oposición y portabilidad. Ejerce esos derechos contactando a
          quien administra tu organización en Mavora (la cuenta con la que os registrasteis). Este producto no publica un
          email de soporte ni un domicilio: no los inventamos aquí. Si eres esa persona administradora, usa el email de
          tu cuenta.
        </p>
      </section>

      <section className="mt-8 space-y-3 text-sm leading-6">
        <h2 className="text-base font-semibold">Cambios</h2>
        <p>Si cambia el tratamiento, actualizaremos esta página.</p>
      </section>

      <p className="mt-10 text-sm text-zinc-500">
        <Link className="text-zinc-900 underline dark:text-zinc-100" to="/login">
          Entrar en Mavora
        </Link>
      </p>
    </main>
  );
}
