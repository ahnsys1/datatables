import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  turbopack: {
    root: process.cwd(),
  },
  async rewrites() {
    return [{
      source: "/api/admin/:path*",
      destination: `${process.env.ADMIN_BACKEND_URL ?? "http://localhost:8090"}/api/admin/:path*`,
    }];
  },
};

export default nextConfig;
